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
            java.util.List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();
            for (Long id : skinTypeIds) {
                jakarta.persistence.criteria.Subquery<Integer> subquery = query.subquery(Integer.class);
                jakarta.persistence.criteria.Root<Product> subRoot = subquery.from(Product.class);
                Join<Product, SkinType> subJoin = subRoot.join("skinTypes");
                subquery.select(cb.literal(1));
                subquery.where(cb.and(
                    cb.equal(subRoot.get("productId"), root.get("productId")),
                    cb.equal(subJoin.get("skinTypeId"), id)
                ));
                predicates.add(cb.exists(subquery));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    public static Specification<Product> hasConcernIn(java.util.List<Long> concernIds) {
        return (root, query, cb) -> {
            if (concernIds == null || concernIds.isEmpty()) return null;
            java.util.List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();
            for (Long id : concernIds) {
                jakarta.persistence.criteria.Subquery<Integer> subquery = query.subquery(Integer.class);
                jakarta.persistence.criteria.Root<Product> subRoot = subquery.from(Product.class);
                Join<Product, SkinConcern> subJoin = subRoot.join("concerns");
                subquery.select(cb.literal(1));
                subquery.where(cb.and(
                    cb.equal(subRoot.get("productId"), root.get("productId")),
                    cb.equal(subJoin.get("concernId"), id)
                ));
                predicates.add(cb.exists(subquery));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    public static Specification<Product> hasIngredientIn(java.util.List<String> ingredients) {
        return (root, query, cb) -> {
            if (ingredients == null || ingredients.isEmpty()) return null;
            java.util.List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();
            for (String name : ingredients) {
                jakarta.persistence.criteria.Subquery<Integer> subquery = query.subquery(Integer.class);
                jakarta.persistence.criteria.Root<Product> subRoot = subquery.from(Product.class);
                Join<Product, com.example.demo.entity.Ingredient> subJoin = subRoot.join("ingredients");
                subquery.select(cb.literal(1));
                subquery.where(cb.and(
                    cb.equal(subRoot.get("productId"), root.get("productId")),
                    cb.equal(subJoin.get("name"), name)
                ));
                predicates.add(cb.exists(subquery));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    public static Specification<Product> priceGreaterThanOrEqual(BigDecimal minPrice) {
        return (root, query, cb) -> minPrice == null ? null : cb.greaterThanOrEqualTo(root.get("price"), minPrice);
    }

    public static Specification<Product> priceLessThanOrEqual(BigDecimal maxPrice) {
        return (root, query, cb) -> maxPrice == null ? null : cb.lessThanOrEqualTo(root.get("price"), maxPrice);
    }
}
