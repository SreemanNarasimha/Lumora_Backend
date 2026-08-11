package com.example.demo.controller;

import com.example.demo.entity.Coupon;
import com.example.demo.repository.CouponRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/coupons")
@PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'ADMIN')")
public class AdminCouponController {

    private final CouponRepository couponRepository;

    public AdminCouponController(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    @GetMapping
    public ResponseEntity<List<Coupon>> getAllCoupons() {
        return ResponseEntity.ok(couponRepository.findAll());
    }

    @PostMapping
    @Transactional
    public ResponseEntity<Coupon> createCoupon(@RequestBody Coupon coupon) {
        if (coupon.getCode() != null) {
            coupon.setCode(coupon.getCode().toUpperCase());
        }
        return ResponseEntity.ok(couponRepository.save(coupon));
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<Coupon> updateCoupon(@PathVariable Integer id, @RequestBody Coupon request) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coupon not found"));
        
        coupon.setCode(request.getCode() != null ? request.getCode().toUpperCase() : null);
        coupon.setDiscountType(request.getDiscountType());
        coupon.setDiscountValue(request.getDiscountValue());
        coupon.setMinOrderAmount(request.getMinOrderAmount());
        coupon.setValidFrom(request.getValidFrom());
        coupon.setValidUntil(request.getValidUntil());
        coupon.setUsageLimit(request.getUsageLimit());
        coupon.setActive(request.isActive());
        
        return ResponseEntity.ok(couponRepository.save(coupon));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> deleteCoupon(@PathVariable Integer id) {
        couponRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
