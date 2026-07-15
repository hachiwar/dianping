package org.com.dianping.event;
import org.com.dianping.entity.ProcessedMessage;
import org.com.dianping.repository.ProcessedMessageRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import java.io.IOException;
@Component
public class OrderEventConsumer {
 private final ProcessedMessageRepository processed;
 private final TransactionTemplate transactions;
 public OrderEventConsumer(ProcessedMessageRepository processed, PlatformTransactionManager transactionManager) {
   this.processed = processed; this.transactions = new TransactionTemplate(transactionManager);
 }
 @RabbitListener(queues = "order.created", containerFactory = "manualRabbitListenerFactory")
 public void consume(OrderCreated event, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
   for (int attempt = 1; attempt <= 3; attempt++) {
     try {
       transactions.executeWithoutResult(status -> {
         if (event.messageId() == null || event.messageId().isBlank()) throw new IllegalArgumentException("messageId is required");
         if (!processed.existsByMessageId(event.messageId())) processed.save(new ProcessedMessage(event.messageId()));
       });
       channel.basicAck(deliveryTag, false);
       return;
     } catch (RuntimeException ex) {
       if (attempt == 3) channel.basicNack(deliveryTag, false, false);
     }
   }
 }
}
