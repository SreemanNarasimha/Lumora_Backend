package com.example.demo.controller;

import com.example.demo.entity.Coupon;
import com.example.demo.repository.CouponRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/coupons")
public class CouponController {

    private final CouponRepository couponRepository;

    public CouponController(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validateCoupon(@RequestBody Map<String, Object> request) {
        String code = (String) request.get("code");
        if (code == null) return ResponseEntity.badRequest().body("Code is required");

        BigDecimal cartTotal = new BigDecimal(request.getOrDefault("cartTotal", "0").toString());

        Optional<Coupon> optCoupon = couponRepository.findByCode(code.toUpperCase());
        if (optCoupon.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid coupon code");
        }

        Coupon coupon = optCoupon.get();

        if (!coupon.isActive()) {
            return ResponseEntity.badRequest().body("Coupon is no longer active");
        }

        LocalDateTime now = LocalDateTime.now();
        if (coupon.getValidFrom() != null && now.isBefore(coupon.getValidFrom())) {
            return ResponseEntity.badRequest().body("Coupon is not valid yet");
        }
        if (coupon.getValidUntil() != null && now.isAfter(coupon.getValidUntil())) {
            return ResponseEntity.badRequest().body("Coupon has expired");
        }

        if (coupon.getUsageLimit() != null && coupon.getTimesUsed() >= coupon.getUsageLimit()) {
            return ResponseEntity.badRequest().body("Coupon usage limit reached");
        }

        if (coupon.getMinOrderAmount() != null && cartTotal.compareTo(coupon.getMinOrderAmount()) < 0) {
            return ResponseEntity.badRequest().body("Minimum order amount not reached (₹" + coupon.getMinOrderAmount() + ")");
        }

        BigDecimal discountAmount = BigDecimal.ZERO;
        if ("PERCENTAGE".equalsIgnoreCase(coupon.getDiscountType())) {
            discountAmount = cartTotal.multiply(coupon.getDiscountValue().divide(new BigDecimal("100")));
        } else if ("FIXED".equalsIgnoreCase(coupon.getDiscountType())) {
            discountAmount = coupon.getDiscountValue();
        }

        // Don't discount more than the cart total
        if (discountAmount.compareTo(cartTotal) > 0) {
            discountAmount = cartTotal;
        }

        Map<String, Object> response = new HashMap<>();
        response.put("valid", true);
        response.put("discountAmount", discountAmount);
        response.put("finalTotal", cartTotal.subtract(discountAmount));
        response.put("couponId", coupon.getId());
        
        return ResponseEntity.ok(response);
    }
}
