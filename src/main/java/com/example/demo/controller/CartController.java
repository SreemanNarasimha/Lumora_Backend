package com.example.demo.controller;

import com.example.demo.dto.CartItemDto;
import com.example.demo.dto.MessageResponse;
import com.example.demo.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    private Integer getUserId(Authentication authentication) {
        return (Integer) authentication.getPrincipal();
    }

    @GetMapping
    public ResponseEntity<List<CartItemDto>> getCart(Authentication authentication) {
        return ResponseEntity.ok(cartService.getCart(getUserId(authentication)));
    }

    @PostMapping
    public ResponseEntity<CartItemDto> addToCart(Authentication authentication, @RequestBody Map<String, Integer> payload) {
        return ResponseEntity.ok(cartService.addToCart(getUserId(authentication), payload.get("productId"), payload.get("quantity")));
    }

    @PutMapping("/{itemId}")
    public ResponseEntity<CartItemDto> updateQuantity(Authentication authentication, @PathVariable Integer itemId, @RequestBody Map<String, Integer> payload) {
        return ResponseEntity.ok(cartService.updateQuantity(getUserId(authentication), itemId, payload.get("quantity")));
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<MessageResponse> removeFromCart(Authentication authentication, @PathVariable Integer itemId) {
        cartService.removeFromCart(getUserId(authentication), itemId);
        return ResponseEntity.ok(new MessageResponse("Item removed from cart"));
    }
}
