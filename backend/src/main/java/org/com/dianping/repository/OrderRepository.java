package org.com.dianping.repository;

import java.util.List;
import java.util.Optional;

import org.com.dianping.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserIdOrderByCreateTimeDesc(Long userId);

    boolean existsByUserId(Long userId);

    List<Order> findByUserId(Long userId);

    boolean existsByVoucherCode(String voucherCode);  // 添加这个方法
    Optional<Order> findByUserIdAndIdempotencyKey(Long userId, String idempotencyKey);
}
