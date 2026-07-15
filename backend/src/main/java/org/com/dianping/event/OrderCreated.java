package org.com.dianping.event;

import java.time.Instant;
import java.util.UUID;
public record OrderCreated(String messageId, String eventType, Long businessId, Instant occurredAt) {
    public static OrderCreated of(Long orderId) { return new OrderCreated(UUID.randomUUID().toString(), "OrderCreated", orderId, Instant.now()); }
}
