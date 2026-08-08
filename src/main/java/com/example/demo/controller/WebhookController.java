package com.example.demo.controller;

import com.example.demo.repository.OrderRepository;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks")
public class WebhookController {

    private final OrderRepository orderRepository;
    private final com.example.demo.repository.ProductRepository productRepository;
    private final com.example.demo.repository.CartItemRepository cartItemRepository;

    @Value("${lumiere.razorpay.webhook-secret}")
    private String webhookSecret;

    public WebhookController(OrderRepository orderRepository, com.example.demo.repository.ProductRepository productRepository, com.example.demo.repository.CartItemRepository cartItemRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.cartItemRepository = cartItemRepository;
    }

    @PostMapping("/razorpay")
    @Transactional
    public ResponseEntity<String> handleRazorpayWebhook(@RequestBody String payload,
                                                        @RequestHeader("x-razorpay-signature") String signature) {
        try {
            boolean isValid = Utils.verifyWebhookSignature(payload, signature, webhookSecret);
            if (!isValid) {
                return ResponseEntity.badRequest().body("Invalid signature");
            }

            JSONObject json = new JSONObject(payload);
            String event = json.getString("event");
            
            if ("payment.captured".equals(event)) {
                JSONObject payment = json.getJSONObject("payload").getJSONObject("payment").getJSONObject("entity");
                String razorpayOrderId = payment.getString("order_id");
                String razorpayPaymentId = payment.getString("id");

                orderRepository.findByRazorpayOrderId(razorpayOrderId).ifPresent(order -> {
                    if (!"PAID".equals(order.getPaymentStatus())) {
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
                    }
                    order.setPaymentStatus("PAID");
                    order.setStatus("PROCESSING");
                    order.setRazorpayPaymentId(razorpayPaymentId);
                    orderRepository.save(order);
                    
                    if (order.getUser() != null) {
                        cartItemRepository.deleteByUserUserId(order.getUser().getUserId());
                    }
                });
            }

            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Webhook Error");
        }
    }
}
