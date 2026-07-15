package org.com.dianping.event;
import org.com.dianping.entity.ProcessedMessage;
import org.com.dianping.repository.ProcessedMessageRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
@Component
public class OrderEventConsumer {
 private final ProcessedMessageRepository processed;
 public OrderEventConsumer(ProcessedMessageRepository processed) { this.processed = processed; }
 @RabbitListener(queues = "order.created") @Transactional
 public void consume(OrderCreated event) { if (!processed.existsByMessageId(event.messageId())) processed.save(new ProcessedMessage(event.messageId())); }
}
