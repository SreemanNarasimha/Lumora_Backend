package com.example.demo.controller;

import com.example.demo.dto.ProductDto;
import com.example.demo.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<Page<ProductDto>> getProducts(
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) Long skinTypeId,
            @RequestParam(required = false) Long concernId,
            @RequestParam(required = false) String ingredient,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy) {
        return ResponseEntity.ok(productService.getProducts(categoryId, brandId, skinTypeId, concernId, ingredient, minPrice, maxPrice, page, size, sortBy));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ProductDto> getProduct(@PathVariable String slug) {
        return ResponseEntity.ok(productService.getProductBySlug(slug));
    }

    @GetMapping("/{id}/recommendations")
    public ResponseEntity<java.util.List<ProductDto>> getRecommendations(@PathVariable Integer id) {
        return ResponseEntity.ok(productService.getRecommendations(id));
    }
}
