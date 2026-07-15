package org.com.dianping.event;

import java.time.Instant;
import java.util.UUID;
import java.util.Map;
public record OrderCreated(String messageId, String eventType, Long businessId, Instant occurredAt, Map<String, Object> payload) {
    public static OrderCreated of(Long orderId) { return new OrderCreated(UUID.randomUUID().toString(), "OrderCreated", orderId, Instant.now(), Map.of("orderId", orderId)); }
}
