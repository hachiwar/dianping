package org.com.dianping.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "order_notification", uniqueConstraints =
        @UniqueConstraint(name = "uk_order_notification_order", columnNames = "order_id"))
public class OrderNotification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "order_id", nullable = false) private Long orderId;
    @Column(name = "user_id", nullable = false) private Long userId;
    @Column(nullable = false, length = 500) private String content;
    @Column(nullable = false) private LocalDateTime createdAt;

    public OrderNotification() { }
    public OrderNotification(Long orderId, Long userId, String content) {
        this.orderId = orderId; this.userId = userId; this.content = content;
        this.createdAt = LocalDateTime.now();
    }
    public Long getId() { return id; }
    public Long getOrderId() { return orderId; }
    public Long getUserId() { return userId; }
    public String getContent() { return content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
