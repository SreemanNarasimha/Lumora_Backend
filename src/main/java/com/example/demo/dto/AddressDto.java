package com.example.demo.dto;
import lombok.Data;

@Data
public class AddressDto {
    private Long addressId;
    private String label;
    private String line1;
    private String line2;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private String phone;
    private Boolean isDefault;
}
