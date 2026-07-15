package org.com.dianping.event;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.*;

@Component
public class OrderEventPublisher {
    private final RabbitTemplate rabbit;
    public OrderEventPublisher(RabbitTemplate rabbit) { this.rabbit = rabbit; }
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publish(OrderCreated event) { try { rabbit.convertAndSend("dianping.events", "order.created", event); } catch (RuntimeException ignored) { } }
}
