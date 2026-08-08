package com.example.demo.dto;
import jakarta.validation.constraints.*;
import lombok.Data;
@Data
public class SendOtpRequest {
    @Email @NotBlank
    private String email;
}
