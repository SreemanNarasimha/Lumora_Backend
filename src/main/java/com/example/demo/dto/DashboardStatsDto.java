package com.example.demo.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDto {
    private BigDecimal totalRevenue;
    private long totalOrders;
    private long totalCustomers;
    private long totalProducts;
    private long lowStockProducts;
    private long pendingRefunds;
    private java.util.List<java.util.Map<String, Object>> chartData;
    private java.util.List<java.util.Map<String, Object>> topProducts;
}
