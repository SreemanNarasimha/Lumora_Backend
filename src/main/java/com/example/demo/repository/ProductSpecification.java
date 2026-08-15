package com.example.demo.repository;

import com.example.demo.entity.Product;
import com.example.demo.entity.SkinType;
import com.example.demo.entity.SkinConcern;
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

    public static Specification<Product> hasSkinTypeIn(java.util.List<Long> skinTypeIds) {
        return (root, query, cb) -> {
            if (skinTypeIds == null || skinTypeIds.isEmpty()) return null;
            Join<Product, SkinType> skinTypes = root.join("skinTypes");
            return skinTypes.get("skinTypeId").in(skinTypeIds);
        };
    }

    public static Specification<Product> hasConcernIn(java.util.List<Long> concernIds) {
        return (root, query, cb) -> {
            if (concernIds == null || concernIds.isEmpty()) return null;
            Join<Product, SkinConcern> concerns = root.join("concerns");
            return concerns.get("concernId").in(concernIds);
        };
    }

    public static Specification<Product> hasIngredientIn(java.util.List<String> ingredients) {
        return (root, query, cb) -> {
            if (ingredients == null || ingredients.isEmpty()) return null;
            // Join with OR conditions for multiple ingredients using like
            java.util.List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();
            for (String ingredient : ingredients) {
                if (ingredient != null && !ingredient.trim().isEmpty()) {
                    predicates.add(cb.like(cb.lower(root.get("description")), "%" + ingredient.toLowerCase() + "%"));
                }
            }
            return cb.or(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    public static Specification<Product> priceGreaterThanOrEqual(BigDecimal minPrice) {
        return (root, query, cb) -> minPrice == null ? null : cb.greaterThanOrEqualTo(root.get("price"), minPrice);
    }

    public static Specification<Product> priceLessThanOrEqual(BigDecimal maxPrice) {
        return (root, query, cb) -> maxPrice == null ? null : cb.lessThanOrEqualTo(root.get("price"), maxPrice);
    }
}
