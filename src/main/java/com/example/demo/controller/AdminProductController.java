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
    public ResponseEntity<?> deleteProduct(@PathVariable Integer id) {
        productRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
