package org.com.dianping.controller;

import java.util.Map;
import jakarta.validation.Valid;
import org.com.dianping.DTO.OrderRequest;
import org.com.dianping.entity.Order;
import org.com.dianping.service.OrderService;
import org.com.dianping.security.CurrentUser;
import org.com.dianping.repository.OrderNotificationRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;
    private final OrderNotificationRepository notifications;
    public OrderController(OrderService orderService, OrderNotificationRepository notifications) {
        this.orderService = orderService; this.notifications = notifications;
    }
    @PostMapping
    public ResponseEntity<?> createOrder(@Valid @RequestBody OrderRequest request) {
        Long userId = CurrentUser.id();
        try {
            Order order = orderService.createOrder(userId, request.packageId(), request.businessId(), request.invitationCode(), request.idempotencyKey());
            return ResponseEntity.ok(Map.of("orderId", order.getId(), "businessNo", order.getBusinessNo(), "message", "订单创建成功"));
        } catch (DataIntegrityViolationException e) {
            Order order = orderService.findByIdempotencyKey(userId, request.idempotencyKey());
            return ResponseEntity.ok(Map.of("orderId", order.getId(), "businessNo", order.getBusinessNo(), "message", "订单创建成功"));
        }
    }
    @PostMapping("/with-invitation-code") public ResponseEntity<?> createOrderWithInvitationCode(@Valid @RequestBody OrderRequest request) { return createOrder(request); }
    @GetMapping("/{orderId}") public ResponseEntity<?> getOrderDetails(@PathVariable Long orderId) { return ResponseEntity.ok(orderService.getOrderDetails(CurrentUser.id(), orderId)); }
    @GetMapping("/user") public ResponseEntity<?> getUserOrders() { return ResponseEntity.ok(orderService.getUserOrders(CurrentUser.id())); }
    @GetMapping("/check-user-orders") public ResponseEntity<Boolean> checkUserOrders() { return ResponseEntity.ok(orderService.checkUserOrders(CurrentUser.id())); }
    @GetMapping("/notifications") public ResponseEntity<?> notifications() {
        return ResponseEntity.ok(notifications.findByUserIdOrderByCreatedAtDesc(CurrentUser.id()));
    }
}
