package com.example.demo.repository;

import com.example.demo.entity.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<WishlistItem, Long> {
    List<WishlistItem> findByUserUserIdOrderByCreatedAtDesc(Integer userId);
    Optional<WishlistItem> findByUserUserIdAndProductProductId(Integer userId, Integer productId);
    boolean existsByUserUserIdAndProductProductId(Integer userId, Integer productId);
    void deleteByUserUserIdAndProductProductId(Integer userId, Integer productId);
    int countByUserUserId(Integer userId);
}
