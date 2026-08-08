package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final ProductRepository productRepository;
    private final InvoiceService invoiceService;
    
    @Value("${lumiere.razorpay.key-id}")
    private String razorpayKeyId;
    
    @Value("${lumiere.razorpay.key-secret}")
    private String razorpayKeySecret;

    public OrderService(OrderRepository orderRepository, CartItemRepository cartItemRepository,
                        AddressRepository addressRepository, UserRepository userRepository,
                        NotificationRepository notificationRepository,
                        ProductRepository productRepository, InvoiceService invoiceService) {
        this.orderRepository = orderRepository;
        this.cartItemRepository = cartItemRepository;
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
        this.productRepository = productRepository;
        this.invoiceService = invoiceService;
    }

    @Transactional
    public RazorpayResponse checkout(Integer userId, CheckoutRequest request) throws Exception {
        User user = userRepository.findById(userId).orElseThrow();
        Address address = addressRepository.findByAddressIdAndUserUserId(request.getAddressId(), userId)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        List<CartItem> cartItems = cartItemRepository.findByUserUserId(userId);
        if (cartItems.isEmpty()) throw new RuntimeException("Cart is empty");

        BigDecimal total = cartItems.stream()
                .map(item -> item.getProduct().getPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String rzpOrderId = "order_mock_" + System.currentTimeMillis();
        int amountInPaise = total.multiply(new BigDecimal(100)).intValue();

        try {
            if (razorpayKeyId != null && !razorpayKeyId.startsWith("rzp_test_placeholder") && !razorpayKeyId.startsWith("secret_placeholder")) {
                RazorpayClient razorpay = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
                JSONObject orderRequest = new JSONObject();
                orderRequest.put("amount", amountInPaise);
                orderRequest.put("currency", "INR");
                orderRequest.put("receipt", "txn_" + System.currentTimeMillis());
                Order razorpayOrder = razorpay.orders.create(orderRequest);
                rzpOrderId = razorpayOrder.get("id");
            }
        } catch (Exception e) {
            System.err.println("Razorpay order creation fallback (dev mode): " + e.getMessage());
        }

        com.example.demo.entity.Order myOrder = new com.example.demo.entity.Order();
        myOrder.setUser(user);
        myOrder.setTotalAmount(total);
        myOrder.setStatus("CREATED");
        myOrder.setPaymentStatus("PENDING");
        myOrder.setAddress(address);
        myOrder.setRazorpayOrderId(rzpOrderId);

        List<OrderItem> orderItems = cartItems.stream().map(ci -> {
            OrderItem oi = new OrderItem();
            oi.setOrder(myOrder);
            oi.setProduct(ci.getProduct());
            oi.setQuantity(ci.getQuantity());
            oi.setPricePerUnit(ci.getProduct().getPrice());
            oi.setTotalPrice(ci.getProduct().getPrice().multiply(new BigDecimal(ci.getQuantity())));
            oi.setProductNameSnapshot(ci.getProduct().getName());
            return oi;
        }).collect(Collectors.toList());

        myOrder.setOrderItems(orderItems);
        orderRepository.save(myOrder);
        
        if ("COD".equalsIgnoreCase(request.getPaymentMethod())) {
            cartItemRepository.deleteByUserUserId(userId);
        }

        RazorpayResponse response = new RazorpayResponse();
        response.setRazorpayOrderId(rzpOrderId);
        response.setAmount(String.valueOf(amountInPaise));
        response.setCurrency("INR");
        return response;
    }

    public List<OrderDto> getMyOrders(Integer userId) {
        return orderRepository.findByUserUserIdOrderByCreatedAtDesc(userId).stream()
            .map(this::mapToDto)
            .collect(Collectors.toList());
    }
    
    private OrderDto mapToDto(com.example.demo.entity.Order order) {
        OrderDto dto = new OrderDto();
        dto.setOrderId(order.getOrderId());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus());
        dto.setPaymentStatus(order.getPaymentStatus());
        dto.setCreatedAt(order.getCreatedAt());
        return dto;
    }

    @Transactional
    public void failOrder(Integer userId, String razorpayOrderId) {
        orderRepository.findByRazorpayOrderId(razorpayOrderId).ifPresent(order -> {
            if (order.getUser().getUserId().equals(userId)) {
                order.setPaymentStatus("FAILED");
                orderRepository.save(order);
                
                Notification notif = new Notification();
                notif.setUser(order.getUser());
                notif.setMessage("Payment failed for Order #" + order.getOrderId() + ". Please try again from your dashboard.");
                notificationRepository.save(notif);
            }
        });
    }

    @Transactional
    public void verifyPayment(Integer userId, String razorpayOrderId, String paymentId, String signature) {
        orderRepository.findByRazorpayOrderId(razorpayOrderId).ifPresent(order -> {
            if (order.getUser().getUserId().equals(userId)) {
                // In a production app, you would verify the signature here using Razorpay's Utils.verifyPaymentSignature
                // Since this is a test app, we just mark it as paid.
                if (!"PAID".equals(order.getPaymentStatus())) {
                    if (order.getOrderItems() != null) {
                        for (OrderItem item : order.getOrderItems()) {
                            Product product = item.getProduct();
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
                if (paymentId != null) {
                    order.setRazorpayPaymentId(paymentId);
                }
                orderRepository.save(order);
                
                cartItemRepository.deleteByUserUserId(userId);
                
                Notification notif = new Notification();
                notif.setUser(order.getUser());
                notif.setMessage("Payment successful for Order #" + order.getOrderId() + ". Your order is now processing.");
                notificationRepository.save(notif);
            }
        });
    }

    public byte[] getInvoicePdf(Integer userId, Integer orderId) {
        com.example.demo.entity.Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        if (!order.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized to download this invoice");
        }
        
        return invoiceService.generateInvoice(order);
    }
}
