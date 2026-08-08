package com.example.demo.controller;

import com.example.demo.dto.DashboardStatsDto;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public AdminDashboardController(UserRepository userRepository,
                                    ProductRepository productRepository,
                                    OrderRepository orderRepository) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'ADMIN', 'PRODUCT_MANAGER', 'ORDER_MANAGER', 'SUPPORT_STAFF')")
    public ResponseEntity<DashboardStatsDto> getStats() {
        long totalCustomers = userRepository.count();
        long totalProducts = productRepository.count();
        long totalOrders = orderRepository.count();
        
        BigDecimal totalRevenue = orderRepository.sumTotalRevenue();
        long lowStockProducts = productRepository.countByStockLessThan(3);
        long pendingRefunds = orderRepository.countByPaymentStatus("REFUND_PENDING");

        java.util.List<Object[]> rawChartData = orderRepository.getRevenueChartData();
        java.util.List<java.util.Map<String, Object>> chartData = new java.util.ArrayList<>();
        for (Object[] row : rawChartData) {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("date", row[0].toString());
            map.put("revenue", row[1]);
            map.put("productsSold", row[2]);
            chartData.add(map);
        }

        DashboardStatsDto stats = new DashboardStatsDto(
                totalRevenue,
                totalOrders,
                totalCustomers,
                totalProducts,
                lowStockProducts,
                pendingRefunds,
                chartData
        );

        return ResponseEntity.ok(stats);
    }
}
