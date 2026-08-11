package com.example.demo.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class AdminProductRequest {
    private String name;
    private String description;
    private BigDecimal price;
    private Integer categoryId;
    private Long brandId;
    private Long skinTypeId;
    private Integer stock;
    private String sku;
    private String barcode;
    private List<String> images;
}
