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
import com.example.demo.service.AuditService;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/admin/orders")
@PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'ADMIN', 'ORDER_MANAGER')")
public class AdminOrderController {

    private final OrderRepository orderRepository;
    private final com.example.demo.repository.ProductRepository productRepository;
    private final com.example.demo.repository.UserRepository userRepository;
    private final AuditService auditService;

    public AdminOrderController(OrderRepository orderRepository, 
                                com.example.demo.repository.ProductRepository productRepository,
                                com.example.demo.repository.UserRepository userRepository,
                                AuditService auditService) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    @GetMapping
    public ResponseEntity<List<OrderDto>> getAllOrders() {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 500, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<Order> orders = orderRepository.findAll(pageable).getContent();
        List<OrderDto> dtos = orders.stream().map(order -> {
            OrderDto dto = new OrderDto();
            dto.setOrderId(order.getOrderId());
            dto.setTotalAmount(order.getTotalAmount());
            dto.setStatus(order.getStatus());
            dto.setPaymentStatus(order.getPaymentStatus());
            dto.setCreatedAt(order.getCreatedAt());
            dto.setTrackingNumber(order.getTrackingNumber());
            dto.setCourierName(order.getCourierName());
            dto.setRefundStatus(order.getRefundStatus());
            dto.setRefundReason(order.getRefundReason());
            if (order.getUser() != null) {
                dto.setUserEmail(order.getUser().getEmail());
            }
            return dto;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PutMapping("/{id}/status")
    @Transactional
    public ResponseEntity<?> updateOrderStatus(Authentication authentication, @PathVariable Integer id, @RequestBody Map<String, String> request) {
        String newStatus = request.get("status");
        if (newStatus == null || newStatus.isEmpty()) {
            return ResponseEntity.badRequest().body("Status is required");
        }
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(newStatus);
        orderRepository.save(order);

        // Log the action
        String adminEmail = authentication != null ? authentication.getName() : "System";
        auditService.logAction(null, adminEmail, "UPDATE_ORDER_STATUS", "Order ID: " + id + " changed to " + newStatus);

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
            
            // Calculate and award Loyalty Points (1 point per ₹100 spent)
            if (order.getUser() != null && order.getTotalAmount() != null) {
                int points = order.getTotalAmount().divide(new java.math.BigDecimal("100"), java.math.RoundingMode.DOWN).intValue();
                order.setPointsEarned(points);
                com.example.demo.entity.User user = order.getUser();
                user.setLoyaltyPoints((user.getLoyaltyPoints() != null ? user.getLoyaltyPoints() : 0) + points);
                userRepository.save(user);
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

    @PutMapping("/{id}/shipping")
    @Transactional
    public ResponseEntity<?> updateOrderShipping(@PathVariable Integer id, @RequestBody Map<String, String> request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        if (request.containsKey("trackingNumber")) {
            order.setTrackingNumber(request.get("trackingNumber"));
        }
        if (request.containsKey("courierName")) {
            order.setCourierName(request.get("courierName"));
        }
        if (request.containsKey("status")) {
            order.setStatus(request.get("status"));
        }
        
        orderRepository.save(order);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/refund")
    @Transactional
    public ResponseEntity<?> processRefund(@PathVariable Integer id, @RequestBody Map<String, String> request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        String action = request.get("action"); // "APPROVE" or "REJECT"
        String reason = request.get("reason");
        
        if ("APPROVE".equalsIgnoreCase(action)) {
            order.setRefundStatus("REFUNDED");
            order.setPaymentStatus("REFUNDED");
            
            // Restock items upon successful refund
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
        } else if ("REJECT".equalsIgnoreCase(action)) {
            order.setRefundStatus("REJECTED");
        } else if ("REQUEST".equalsIgnoreCase(action)) {
            order.setRefundStatus("REQUESTED");
        }
        
        if (reason != null && !reason.isEmpty()) {
            order.setRefundReason(reason);
        }
        
        orderRepository.save(order);
        return ResponseEntity.ok().build();
    }
}
