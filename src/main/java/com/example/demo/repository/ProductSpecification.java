package com.example.demo.repository;

import com.example.demo.entity.Product;
import com.example.demo.entity.SkinType;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;
import java.math.BigDecimal;

public class ProductSpecification {

    public static Specification<Product> hasCategory(Integer categoryId) {
        return (root, query, cb) -> categoryId == null ? null : cb.equal(root.get("category").get("categoryId"), categoryId);
    }

    public static Specification<Product> hasBrand(Long brandId) {
        return (root, query, cb) -> brandId == null ? null : cb.equal(root.get("brand").get("brandId"), brandId);
    }

    public static Specification<Product> hasSkinType(Long skinTypeId) {
        return (root, query, cb) -> {
            if (skinTypeId == null) return null;
            Join<Product, SkinType> skinTypes = root.join("skinTypes");
            return cb.equal(skinTypes.get("skinTypeId"), skinTypeId);
        };
    }

    public static Specification<Product> priceGreaterThanOrEqual(BigDecimal minPrice) {
        return (root, query, cb) -> minPrice == null ? null : cb.greaterThanOrEqualTo(root.get("price"), minPrice);
    }

    public static Specification<Product> priceLessThanOrEqual(BigDecimal maxPrice) {
        return (root, query, cb) -> maxPrice == null ? null : cb.lessThanOrEqualTo(root.get("price"), maxPrice);
    }
}
