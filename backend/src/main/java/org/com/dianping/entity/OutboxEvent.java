package org.com.dianping.entity;

import java.time.Instant;
import jakarta.persistence.*;

@Entity
@Table(name = "outbox_event", uniqueConstraints = @UniqueConstraint(columnNames = "message_id"))
public class OutboxEvent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "message_id", nullable = false) private String messageId;
    @Column(nullable = false) private String eventType;
    @Column(nullable = false) private Long businessId;
    @Column(nullable = false) private Instant occurredAt;
    public OutboxEvent() { }
    public OutboxEvent(String messageId, String eventType, Long businessId, Instant occurredAt) { this.messageId = messageId; this.eventType = eventType; this.businessId = businessId; this.occurredAt = occurredAt; }
    public String getMessageId() { return messageId; }
    public String getEventType() { return eventType; }
    public Long getBusinessId() { return businessId; }
    public Instant getOccurredAt() { return occurredAt; }
}
