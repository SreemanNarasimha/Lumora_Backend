package com.example.demo.repository;
import com.example.demo.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface CartItemRepository extends JpaRepository<CartItem, Integer> {
    List<CartItem> findByUserUserId(Integer userId);
    Optional<CartItem> findByIdAndUserUserId(Integer id, Integer userId);
    Optional<CartItem> findByUserUserIdAndProductProductId(Integer userId, Integer productId);
    void deleteByUserUserId(Integer userId);
}
