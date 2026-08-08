package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payment_methods")
@Getter
@Setter
@NoArgsConstructor
public class PaymentMethod {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_method_id")
    private Long paymentMethodId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "razorpay_customer_id", nullable = false)
    private String razorpayCustomerId;

    @Column(name = "razorpay_token_id", nullable = false)
    private String razorpayTokenId;

    @Column(name = "card_brand")
    private String cardBrand;

    @Column(name = "card_last4", length = 4)
    private String cardLast4;

    @Column(name = "card_expiry_month")
    private Byte cardExpiryMonth;

    @Column(name = "card_expiry_year")
    private Short cardExpiryYear;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;
}
