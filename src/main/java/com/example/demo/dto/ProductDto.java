package com.example.demo.dto;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductDto {
    private Integer productId;
    private String name;
    private String description;
    private BigDecimal price;
    private String slug;
    private BigDecimal rating;
    private String categoryName;
    private String brandName;
    private Integer stock;
    private String sku;
    private String barcode;
    private List<String> images;
}
