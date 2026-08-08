package com.example.demo.controller;

import com.example.demo.entity.Product;
import com.example.demo.entity.User;
import com.example.demo.entity.WishlistItem;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.WishlistRepository;
import com.example.demo.service.ProductService;
import com.example.demo.dto.MessageResponse;
import com.example.demo.dto.ProductDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;

    public WishlistController(WishlistRepository wishlistRepository,
                              UserRepository userRepository,
                              ProductRepository productRepository,
                              ProductService productService) {
        this.wishlistRepository = wishlistRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.productService = productService;
    }

    private Integer getUserId(Authentication authentication) {
        return (Integer) authentication.getPrincipal();
    }

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<ProductDto>> getWishlist(Authentication authentication) {
        Integer userId = getUserId(authentication);
        List<WishlistItem> items = wishlistRepository.findByUserUserIdOrderByCreatedAtDesc(userId);
        List<ProductDto> products = items.stream().map(item -> productService.mapToDto(item.getProduct())).collect(Collectors.toList());
        return ResponseEntity.ok(products);
    }

    @PostMapping("/{productId}")
    public ResponseEntity<MessageResponse> addToWishlist(Authentication authentication, @PathVariable Integer productId) {
        Integer userId = getUserId(authentication);
        if (!wishlistRepository.existsByUserUserIdAndProductProductId(userId, productId)) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            WishlistItem item = new WishlistItem();
            item.setUser(user);
            item.setProduct(product);
            wishlistRepository.save(item);
        }
        return ResponseEntity.ok(new MessageResponse("Product added to wishlist"));
    }

    @DeleteMapping("/{productId}")
    @Transactional
    public ResponseEntity<MessageResponse> removeFromWishlist(Authentication authentication, @PathVariable Integer productId) {
        Integer userId = getUserId(authentication);
        wishlistRepository.deleteByUserUserIdAndProductProductId(userId, productId);
        return ResponseEntity.ok(new MessageResponse("Product removed from wishlist"));
    }

    @GetMapping("/check/{productId}")
    public ResponseEntity<Map<String, Boolean>> checkWishlist(Authentication authentication, @PathVariable Integer productId) {
        Integer userId = getUserId(authentication);
        boolean exists = wishlistRepository.existsByUserUserIdAndProductProductId(userId, productId);
        return ResponseEntity.ok(Map.of("inWishlist", exists));
    }
}
