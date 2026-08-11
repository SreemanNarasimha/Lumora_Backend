package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
@Getter
@Setter
@NoArgsConstructor
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String code;

    @Column(nullable = false)
    private String discountType; // "PERCENTAGE" or "FIXED"

    @Column(nullable = false)
    private BigDecimal discountValue;

    private BigDecimal minOrderAmount;

    private LocalDateTime validFrom;

    private LocalDateTime validUntil;

    private Integer usageLimit;

    private Integer timesUsed = 0;

    private boolean active = true;
}
