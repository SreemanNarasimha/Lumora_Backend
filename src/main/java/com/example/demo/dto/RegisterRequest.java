package com.example.demo.dto;
import jakarta.validation.constraints.*;
import lombok.Data;
@Data
public class RegisterRequest {
    @NotBlank
    private String username;
    @NotBlank
    private String fullName;
    @Email @NotBlank
    private String email;
    @NotBlank
    private String phone;
    @NotBlank @Size(min=8)
    private String password;
}
