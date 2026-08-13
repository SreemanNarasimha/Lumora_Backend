package com.example.demo.dto;
import lombok.Data;
@Data
public class RazorpayResponse {
    private String razorpayOrderId;
    private String amount;
    private String currency;
    private String keyId;
}
