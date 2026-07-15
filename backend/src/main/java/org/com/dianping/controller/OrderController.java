package org.com.dianping.controller;

import java.util.Map;
import org.com.dianping.entity.Order;
import org.com.dianping.service.OrderService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;
    public OrderController(OrderService orderService) { this.orderService = orderService; }
    @PostMapping
    public ResponseEntity<?> createOrder(@RequestHeader("UserId") Long userId, @RequestBody Map<String, Object> request) {
        Long packageId = Long.valueOf(request.get("packageId").toString());
        Long merchantId = Long.valueOf(request.get("businessId").toString());
        String key = String.valueOf(request.get("idempotencyKey"));
        try {
            Order order = orderService.createOrder(userId, packageId, merchantId, (String) request.get("invitationCode"), key);
            return ResponseEntity.ok(Map.of("orderId", order.getId(), "businessNo", order.getBusinessNo(), "message", "订单创建成功"));
        } catch (DataIntegrityViolationException e) {
            Order order = orderService.findByIdempotencyKey(userId, key);
            return ResponseEntity.ok(Map.of("orderId", order.getId(), "businessNo", order.getBusinessNo(), "message", "订单创建成功"));
        }
    }
    @PostMapping("/with-invitation-code") public ResponseEntity<?> createOrderWithInvitationCode(@RequestHeader("UserId") Long userId, @RequestBody Map<String, Object> request) { return createOrder(userId, request); }
    @GetMapping("/{orderId}") public ResponseEntity<?> getOrderDetails(@RequestHeader("UserId") Long userId, @PathVariable Long orderId) { return ResponseEntity.ok(orderService.getOrderDetails(userId, orderId)); }
    @GetMapping("/user") public ResponseEntity<?> getUserOrders(@RequestHeader("UserId") Long userId) { return ResponseEntity.ok(orderService.getUserOrders(userId)); }
    @GetMapping("/check-user-orders") public ResponseEntity<Boolean> checkUserOrders(@RequestHeader("UserId") Long userId) { return ResponseEntity.ok(orderService.checkUserOrders(userId)); }
}
