package org.com.dianping.event;
import org.com.dianping.entity.ProcessedMessage;
import org.com.dianping.repository.ProcessedMessageRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import java.io.IOException;
@Component
public class OrderEventConsumer {
 private final ProcessedMessageRepository processed;
 public OrderEventConsumer(ProcessedMessageRepository processed) { this.processed = processed; }
 @RabbitListener(queues = "order.created", containerFactory = "manualRabbitListenerFactory") @Transactional
 public void consume(OrderCreated event, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
   if (!processed.existsByMessageId(event.messageId())) processed.save(new ProcessedMessage(event.messageId()));
   channel.basicAck(deliveryTag, false);
 }
}
