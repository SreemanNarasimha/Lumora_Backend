package com.example.demo.service;

import com.example.demo.dto.CartItemDto;
import com.example.demo.entity.CartItem;
import com.example.demo.entity.Product;
import com.example.demo.entity.User;
import com.example.demo.repository.CartItemRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartService(CartItemRepository cartItemRepository, ProductRepository productRepository, UserRepository userRepository) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public List<CartItemDto> getCart(Integer userId) {
        return cartItemRepository.findByUserUserId(userId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public CartItemDto addToCart(Integer userId, Integer productId, Integer quantity) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        CartItem cartItem = cartItemRepository.findByUserUserIdAndProductProductId(userId, productId)
                .orElseGet(() -> {
                    CartItem newItem = new CartItem();
                    newItem.setUser(user);
                    newItem.setProduct(product);
                    newItem.setQuantity(0);
                    return newItem;
                });

        cartItem.setQuantity(cartItem.getQuantity() + quantity);
        return mapToDto(cartItemRepository.save(cartItem));
    }

    @Transactional
    public CartItemDto updateQuantity(Integer userId, Integer cartItemId, Integer quantity) {
        CartItem cartItem = cartItemRepository.findByIdAndUserUserId(cartItemId, userId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));
        cartItem.setQuantity(quantity);
        return mapToDto(cartItemRepository.save(cartItem));
    }

    @Transactional
    public void removeFromCart(Integer userId, Integer cartItemId) {
        CartItem cartItem = cartItemRepository.findByIdAndUserUserId(cartItemId, userId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));
        cartItemRepository.delete(cartItem);
    }

    private CartItemDto mapToDto(CartItem item) {
        CartItemDto dto = new CartItemDto();
        dto.setId(item.getId());
        dto.setProductId(item.getProduct().getProductId());
        dto.setName(item.getProduct().getName());
        dto.setPrice(item.getProduct().getPrice());
        dto.setQuantity(item.getQuantity());
        if (!item.getProduct().getImages().isEmpty()) {
            dto.setImageUrl(item.getProduct().getImages().get(0).getImageUrl());
        }
        return dto;
    }
}
