package com.example.demo.dto;
import jakarta.validation.constraints.*;
import lombok.Data;
@Data
public class VerifyOtpRequest {
    @Email @NotBlank
    private String email;
    @NotBlank
    private String otp;
}
