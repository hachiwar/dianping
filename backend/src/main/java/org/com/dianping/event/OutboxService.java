package org.com.dianping.event;

import java.util.Map;
import org.com.dianping.entity.OutboxEvent;
import org.com.dianping.repository.OutboxEventRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class OutboxService {
    private static final Logger log = LoggerFactory.getLogger(OutboxService.class);
    private final OutboxEventRepository events;
    private final RabbitTemplate rabbit;
    public OutboxService(OutboxEventRepository events, RabbitTemplate rabbit) { this.events = events; this.rabbit = rabbit; }
    @Transactional
    public void record(OrderCreated event) { events.save(new OutboxEvent(event.messageId(), event.eventType(), event.businessId(), event.occurredAt())); }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void publish(OrderCreated event) {
        CorrelationData confirmation = new CorrelationData(event.messageId());
        try {
            rabbit.convertAndSend("dianping.events", "order.created", event, confirmation);
            if (confirmation.getFuture().get(2, TimeUnit.SECONDS).isAck()) events.deleteByMessageId(event.messageId());
            else log.warn("rabbit rejected outbox messageId={}", event.messageId());
        } catch (Exception ex) {
            if (ex instanceof InterruptedException) Thread.currentThread().interrupt();
            log.warn("rabbit publish deferred for outbox messageId={}", event.messageId(), ex);
        }
    }
    @Scheduled(fixedDelay = 5000)
    public void retryPending() { events.findTop100ByOrderByIdAsc().forEach(value -> publish(new OrderCreated(value.getMessageId(), value.getEventType(), value.getBusinessId(), value.getOccurredAt(), Map.of("orderId", value.getBusinessId())))); }
}
