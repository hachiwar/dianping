package org.com.dianping.repository;

import java.util.List;
import org.com.dianping.entity.OrderNotification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderNotificationRepository extends JpaRepository<OrderNotification, Long> {
    boolean existsByOrderId(Long orderId);
    List<OrderNotification> findByUserIdOrderByCreatedAtDesc(Long userId);
}
