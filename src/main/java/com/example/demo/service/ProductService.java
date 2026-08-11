package com.example.demo.service;

import com.example.demo.dto.ProductDto;
import com.example.demo.entity.Product;
import com.example.demo.entity.ProductImage;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.ProductSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Page<ProductDto> getProducts(Integer categoryId, Long brandId, Long skinTypeId, 
                                        Long concernId, String ingredient,
                                        BigDecimal minPrice, BigDecimal maxPrice, 
                                        int page, int size, String sortBy) {
        Specification<Product> spec = Specification.where((root, query, cb) -> cb.conjunction());
        if (categoryId != null) spec = spec.and(ProductSpecification.hasCategory(categoryId));
        if (brandId != null) spec = spec.and(ProductSpecification.hasBrand(brandId));
        if (skinTypeId != null) spec = spec.and(ProductSpecification.hasSkinType(skinTypeId));
        if (concernId != null) spec = spec.and(ProductSpecification.hasConcern(concernId));
        if (ingredient != null && !ingredient.isEmpty()) spec = spec.and(ProductSpecification.hasIngredient(ingredient));
        if (minPrice != null) spec = spec.and(ProductSpecification.priceGreaterThanOrEqual(minPrice));
        if (maxPrice != null) spec = spec.and(ProductSpecification.priceLessThanOrEqual(maxPrice));

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(sortBy != null ? sortBy : "createdAt").descending());
        
        return productRepository.findAll(spec, pageRequest).map(this::mapToDto);
    }

    public ProductDto getProductBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return mapToDto(product);
    }

    public java.util.List<ProductDto> getRecommendations(Integer productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        if (product.getCategory() == null) {
            return java.util.Collections.emptyList();
        }

        // Find products in the same category, excluding the current product, limit to 4
        return productRepository.findRecommendations(
                product.getCategory().getCategoryId(), 
                productId, 
                PageRequest.of(0, 4)
        ).stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public ProductDto mapToDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.setProductId(product.getProductId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setSlug(product.getSlug());
        dto.setRating(product.getRating());
        dto.setCategoryName(product.getCategory() != null ? product.getCategory().getCategoryName() : null);
        dto.setBrandName(product.getBrand() != null ? product.getBrand().getBrandName() : null);
        dto.setStock(product.getStock() != null ? product.getStock() : 0);
        dto.setSku(product.getSku());
        dto.setBarcode(product.getBarcode());
        dto.setImages(product.getImages().stream().map(ProductImage::getImageUrl).collect(Collectors.toList()));
        return dto;
    }
}
