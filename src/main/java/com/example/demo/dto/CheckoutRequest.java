package com.example.demo.dto;
import lombok.Data;
@Data
public class CheckoutRequest {
    private Long addressId;
    private String paymentMethod;
}
