package com.example.demo.repository;
import com.example.demo.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface OrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findByUserUserIdOrderByCreatedAtDesc(Integer userId);
    Optional<Order> findByOrderIdAndUserUserId(Integer orderId, Integer userId);
    Optional<Order> findByRazorpayOrderId(String razorpayOrderId);

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.paymentStatus = 'PAID'")
    java.math.BigDecimal sumTotalRevenue();

    @org.springframework.data.jpa.repository.Query("SELECT DATE(o.createdAt) as date, SUM(o.totalAmount) as revenue, SUM(i.quantity) as productsSold " +
            "FROM Order o JOIN o.orderItems i " +
            "WHERE o.paymentStatus = 'PAID' " +
            "GROUP BY DATE(o.createdAt) " +
            "ORDER BY DATE(o.createdAt) ASC")
    List<Object[]> getRevenueChartData();

    long countByStatus(String status);
    long countByPaymentStatus(String paymentStatus);

    @org.springframework.data.jpa.repository.Query("SELECT p.name, SUM(i.quantity) as totalSold, p.price, p.stock " +
            "FROM OrderItem i JOIN i.product p JOIN i.order o " +
            "WHERE o.paymentStatus = 'PAID' " +
            "GROUP BY p.productId, p.name, p.price, p.stock " +
            "ORDER BY totalSold DESC")
    List<Object[]> getTopSellingProducts(org.springframework.data.domain.Pageable pageable);
}
