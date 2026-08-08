package com.example.demo.controller;

import com.example.demo.dto.OrderDto;
import com.example.demo.entity.Order;
import com.example.demo.repository.OrderRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/orders")
@PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'ADMIN', 'ORDER_MANAGER')")
public class AdminOrderController {

    private final OrderRepository orderRepository;
    private final com.example.demo.repository.ProductRepository productRepository;

    public AdminOrderController(OrderRepository orderRepository, com.example.demo.repository.ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    @GetMapping
    public ResponseEntity<List<OrderDto>> getAllOrders() {
        List<Order> orders = orderRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        List<OrderDto> dtos = orders.stream().map(order -> {
            OrderDto dto = new OrderDto();
            dto.setOrderId(order.getOrderId());
            dto.setTotalAmount(order.getTotalAmount());
            dto.setStatus(order.getStatus());
            dto.setPaymentStatus(order.getPaymentStatus());
            dto.setCreatedAt(order.getCreatedAt());
            if (order.getUser() != null) {
                dto.setUserEmail(order.getUser().getEmail());
            }
            return dto;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PutMapping("/{id}/status")
    @Transactional
    public ResponseEntity<?> updateOrderStatus(@PathVariable Integer id, @RequestBody Map<String, String> request) {
        String newStatus = request.get("status");
        if (newStatus == null || newStatus.isEmpty()) {
            return ResponseEntity.badRequest().body("Status is required");
        }
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(newStatus);
        orderRepository.save(order);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/payment-status")
    @Transactional
    public ResponseEntity<?> updateOrderPaymentStatus(@PathVariable Integer id, @RequestBody Map<String, String> request) {
        String newPaymentStatus = request.get("paymentStatus");
        if (newPaymentStatus == null || newPaymentStatus.isEmpty()) {
            return ResponseEntity.badRequest().body("Payment Status is required");
        }
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        String oldPaymentStatus = order.getPaymentStatus();
        order.setPaymentStatus(newPaymentStatus);
        
        // Handle stock updates
        if (!"PAID".equals(oldPaymentStatus) && "PAID".equals(newPaymentStatus)) {
            // Deduct stock
            if (order.getOrderItems() != null) {
                for (com.example.demo.entity.OrderItem item : order.getOrderItems()) {
                    com.example.demo.entity.Product product = item.getProduct();
                    if (product != null) {
                        int currentStock = product.getStock() != null ? product.getStock() : 0;
                        product.setStock(Math.max(0, currentStock - item.getQuantity()));
                        productRepository.save(product);
                    }
                }
            }
        } else if ("PAID".equals(oldPaymentStatus) && !"PAID".equals(newPaymentStatus)) {
            // Restore stock
            if (order.getOrderItems() != null) {
                for (com.example.demo.entity.OrderItem item : order.getOrderItems()) {
                    com.example.demo.entity.Product product = item.getProduct();
                    if (product != null) {
                        int currentStock = product.getStock() != null ? product.getStock() : 0;
                        product.setStock(currentStock + item.getQuantity());
                        productRepository.save(product);
                    }
                }
            }
        }
        
        orderRepository.save(order);
        return ResponseEntity.ok().build();
    }
}
