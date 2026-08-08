package com.example.demo.controller;

import com.example.demo.entity.Product;
import com.example.demo.entity.Review;
import com.example.demo.entity.User;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.ReviewRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.dto.MessageResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public ReviewController(ReviewRepository reviewRepository,
                            UserRepository userRepository,
                            ProductRepository productRepository) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    private Integer getUserId(Authentication authentication) {
        return (Integer) authentication.getPrincipal();
    }

    @GetMapping
    public ResponseEntity<List<Review>> getUserReviews(Authentication authentication) {
        Integer userId = getUserId(authentication);
        return ResponseEntity.ok(reviewRepository.findByUserUserIdOrderByCreatedAtDesc(userId));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<Review>> getProductReviews(@PathVariable Integer productId) {
        return ResponseEntity.ok(reviewRepository.findByProductProductIdOrderByCreatedAtDesc(productId));
    }

    @PostMapping
    public ResponseEntity<Review> createReview(Authentication authentication, @RequestBody Map<String, Object> payload) {
        Integer userId = getUserId(authentication);
        Integer productId = ((Number) payload.get("productId")).intValue();
        Integer rating = ((Number) payload.get("rating")).intValue();
        String comment = (String) payload.get("comment");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Review review = new Review();
        review.setUser(user);
        review.setProduct(product);
        review.setRating(rating);
        review.setComment(comment);

        return ResponseEntity.ok(reviewRepository.save(review));
    }
}
