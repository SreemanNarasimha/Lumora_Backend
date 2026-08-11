package com.example.demo.controller;

import com.example.demo.dto.AdminProductRequest;
import com.example.demo.dto.ProductDto;
import com.example.demo.entity.Product;
import com.example.demo.entity.ProductImage;
import com.example.demo.repository.BrandRepository;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.SkinTypeRepository;
import com.example.demo.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/admin/products")
@PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'ADMIN', 'PRODUCT_MANAGER')")
public class AdminProductController {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final SkinTypeRepository skinTypeRepository;
    private final ProductService productService;

    public AdminProductController(ProductRepository productRepository,
                                  CategoryRepository categoryRepository,
                                  BrandRepository brandRepository,
                                  SkinTypeRepository skinTypeRepository,
                                  ProductService productService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
        this.skinTypeRepository = skinTypeRepository;
        this.productService = productService;
    }

    @PostMapping
    @Transactional
    public ResponseEntity<ProductDto> createProduct(@RequestBody AdminProductRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice() != null ? request.getPrice() : BigDecimal.ZERO);
        product.setStock(request.getStock() != null ? request.getStock() : 0);
        product.setRating(BigDecimal.ZERO);
        product.setSlug(request.getName().toLowerCase().replaceAll("[^a-z0-9]+", "-"));
        product.setSku(request.getSku());
        product.setBarcode(request.getBarcode());
        
        if (request.getCategoryId() != null) {
            product.setCategory(categoryRepository.findById(request.getCategoryId()).orElse(null));
        }
        if (request.getBrandId() != null) {
            product.setBrand(brandRepository.findById(request.getBrandId()).orElse(null));
        }
        if (request.getSkinTypeId() != null) {
            skinTypeRepository.findById(request.getSkinTypeId())
                .ifPresent(st -> product.setSkinTypes(List.of(st)));
        }
        
        product.setImages(new ArrayList<>());
        if (request.getImages() != null) {
            for (String url : request.getImages()) {
                ProductImage img = new ProductImage();
                img.setImageUrl(url);
                img.setProduct(product);
                product.getImages().add(img);
            }
        }
        
        Product savedProduct = productRepository.save(product);
        return ResponseEntity.ok(productService.mapToDto(savedProduct));
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<ProductDto> updateProduct(@PathVariable Integer id, @RequestBody AdminProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
                
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        if (request.getPrice() != null) product.setPrice(request.getPrice());
        if (request.getStock() != null) product.setStock(request.getStock());
        product.setSlug(request.getName().toLowerCase().replaceAll("[^a-z0-9]+", "-"));
        product.setSku(request.getSku());
        product.setBarcode(request.getBarcode());
        
        if (request.getCategoryId() != null) {
            product.setCategory(categoryRepository.findById(request.getCategoryId()).orElse(null));
        }
        if (request.getBrandId() != null) {
            product.setBrand(brandRepository.findById(request.getBrandId()).orElse(null));
        }
        if (request.getSkinTypeId() != null) {
            skinTypeRepository.findById(request.getSkinTypeId())
                .ifPresent(st -> product.setSkinTypes(List.of(st)));
        }
        
        product.getImages().clear();
        if (request.getImages() != null) {
            for (String url : request.getImages()) {
                ProductImage img = new ProductImage();
                img.setImageUrl(url);
                img.setProduct(product);
                product.getImages().add(img);
            }
        }
        
        Product savedProduct = productRepository.save(product);
        return ResponseEntity.ok(productService.mapToDto(savedProduct));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> deleteProduct(@PathVariable Integer id) {
        productRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/bulk")
    @Transactional
    public ResponseEntity<String> bulkUploadProducts(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }

        int successCount = 0;
        int errorCount = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean isFirstLine = true;
            
            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false; // Skip header
                    continue;
                }
                
                String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                if (data.length < 4) {
                    errorCount++;
                    continue; // Skip malformed rows
                }
                
                try {
                    Product product = new Product();
                    String name = data[0].replaceAll("^\"|\"$", "").trim();
                    product.setName(name);
                    product.setDescription(data[1].replaceAll("^\"|\"$", "").trim());
                    product.setPrice(new BigDecimal(data[2].trim()));
                    product.setStock(Integer.parseInt(data[3].trim()));
                    product.setRating(BigDecimal.ZERO);
                    product.setSlug(name.toLowerCase().replaceAll("[^a-z0-9]+", "-"));
                    
                    if (data.length > 4 && !data[4].trim().isEmpty()) {
                        categoryRepository.findById(Integer.parseInt(data[4].trim())).ifPresent(product::setCategory);
                    }
                    if (data.length > 5 && !data[5].trim().isEmpty()) {
                        brandRepository.findById(Long.parseLong(data[5].trim())).ifPresent(product::setBrand);
                    }
                    if (data.length > 6 && !data[6].trim().isEmpty()) {
                        skinTypeRepository.findById(Long.parseLong(data[6].trim())).ifPresent(skinType -> product.setSkinTypes(java.util.List.of(skinType)));
                    }
                    
                    Product savedProduct = productRepository.save(product);
                    
                    // Images (comma-separated URLs inside the CSV column)
                    if (data.length > 7 && !data[7].trim().isEmpty()) {
                        String[] urls = data[7].replaceAll("^\"|\"$", "").split(";");
                        List<ProductImage> images = new ArrayList<>();
                        for (String url : urls) {
                            if (!url.trim().isEmpty()) {
                                ProductImage pi = new ProductImage();
                                pi.setImageUrl(url.trim());
                                pi.setProduct(savedProduct);
                                images.add(pi);
                            }
                        }
                        savedProduct.setImages(images);
                        productRepository.save(savedProduct);
                    }
                    successCount++;
                } catch (Exception e) {
                    errorCount++;
                }
            }
            return ResponseEntity.ok(String.format("Bulk upload completed. %d products created successfully. %d rows skipped.", successCount, errorCount));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to process CSV file: " + e.getMessage());
        }
    }
}
