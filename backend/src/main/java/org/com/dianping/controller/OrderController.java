package org.com.dianping.controller;

import java.util.Map;
import org.com.dianping.entity.Order;
import org.com.dianping.service.OrderService;
import org.com.dianping.security.CurrentUser;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;
    public OrderController(OrderService orderService) { this.orderService = orderService; }
    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> request) {
        Long userId = CurrentUser.id();
        Long packageId = Long.valueOf(request.get("packageId").toString());
        Long merchantId = Long.valueOf(request.get("businessId").toString());
        Object keyValue = request.get("idempotencyKey");
        String key = keyValue == null ? null : keyValue.toString();
        try {
            Order order = orderService.createOrder(userId, packageId, merchantId, (String) request.get("invitationCode"), key);
            return ResponseEntity.ok(Map.of("orderId", order.getId(), "businessNo", order.getBusinessNo(), "message", "订单创建成功"));
        } catch (DataIntegrityViolationException e) {
            Order order = orderService.findByIdempotencyKey(userId, key);
            return ResponseEntity.ok(Map.of("orderId", order.getId(), "businessNo", order.getBusinessNo(), "message", "订单创建成功"));
        }
    }
    @PostMapping("/with-invitation-code") public ResponseEntity<?> createOrderWithInvitationCode(@RequestBody Map<String, Object> request) { return createOrder(request); }
    @GetMapping("/{orderId}") public ResponseEntity<?> getOrderDetails(@PathVariable Long orderId) { return ResponseEntity.ok(orderService.getOrderDetails(CurrentUser.id(), orderId)); }
    @GetMapping("/user") public ResponseEntity<?> getUserOrders() { return ResponseEntity.ok(orderService.getUserOrders(CurrentUser.id())); }
    @GetMapping("/check-user-orders") public ResponseEntity<Boolean> checkUserOrders() { return ResponseEntity.ok(orderService.checkUserOrders(CurrentUser.id())); }
}
