package com.example.demo.controller;

import com.example.demo.dto.CheckoutRequest;
import com.example.demo.dto.OrderDto;
import com.example.demo.dto.RazorpayResponse;
import com.example.demo.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    private Integer getUserId(Authentication authentication) {
        return (Integer) authentication.getPrincipal();
    }

    @PostMapping("/checkout")
    public ResponseEntity<RazorpayResponse> checkout(Authentication authentication, @RequestBody CheckoutRequest request) throws Exception {
        return ResponseEntity.ok(orderService.checkout(getUserId(authentication), request));
    }

    @GetMapping
    public ResponseEntity<List<OrderDto>> getOrders(Authentication authentication) {
        return ResponseEntity.ok(orderService.getMyOrders(getUserId(authentication)));
    }

    @PostMapping("/{razorpayOrderId}/fail")
    public ResponseEntity<Void> failOrder(@PathVariable String razorpayOrderId, Authentication authentication) {
        orderService.failOrder(getUserId(authentication), razorpayOrderId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{razorpayOrderId}/verify")
    public ResponseEntity<Void> verifyOrder(@PathVariable String razorpayOrderId, Authentication authentication, @RequestBody java.util.Map<String, String> payload) {
        orderService.verifyPayment(getUserId(authentication), razorpayOrderId, payload.get("razorpayPaymentId"), payload.get("razorpaySignature"));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{orderId}/invoice")
    public ResponseEntity<byte[]> downloadInvoice(@PathVariable Integer orderId, Authentication authentication) {
        byte[] pdfBytes = orderService.getInvoicePdf(getUserId(authentication), orderId);
        
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "invoice_LMR-" + orderId + ".pdf");
        
        return new ResponseEntity<>(pdfBytes, headers, org.springframework.http.HttpStatus.OK);
    }
}
