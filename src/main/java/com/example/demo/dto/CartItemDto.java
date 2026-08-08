package com.example.demo.dto;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CartItemDto {
    private Integer id;
    private Integer productId;
    private String name;
    private BigDecimal price;
    private Integer quantity;
    private String imageUrl;
}
