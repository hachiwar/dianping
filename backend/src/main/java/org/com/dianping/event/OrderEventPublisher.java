package org.com.dianping.event;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.*;

@Component
public class OrderEventPublisher {
    private final OutboxService outbox;
    public OrderEventPublisher(OutboxService outbox) { this.outbox = outbox; }
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publish(OrderCreated event) { outbox.publish(event); }
}
