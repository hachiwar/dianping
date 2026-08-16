package org.com.dianping.event;
import java.security.SecureRandom;
import org.com.dianping.entity.Order;
import org.com.dianping.entity.OrderNotification;
import org.com.dianping.entity.ProcessedMessage;
import org.com.dianping.repository.OrderNotificationRepository;
import org.com.dianping.repository.OrderRepository;
import org.com.dianping.repository.ProcessedMessageRepository;
import org.com.dianping.repository.UserRepository;
import org.com.dianping.service.InvitationService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Component
public class OrderEventConsumer {
 private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);
 private static final SecureRandom RANDOM = new SecureRandom();
 private final ProcessedMessageRepository processed;
 private final OrderRepository orders;
 private final UserRepository users;
 private final InvitationService invitations;
 private final OrderNotificationRepository notifications;
 private final TransactionTemplate transactions;
 public OrderEventConsumer(ProcessedMessageRepository processed, OrderRepository orders,
                           UserRepository users, InvitationService invitations,
                           OrderNotificationRepository notifications,
                           PlatformTransactionManager transactionManager) {
   this.processed = processed; this.orders = orders; this.users = users;
   this.invitations = invitations; this.notifications = notifications;
   this.transactions = new TransactionTemplate(transactionManager);
 }
 @RabbitListener(queues = "order.created", containerFactory = "manualRabbitListenerFactory")
 public void consume(OrderCreated event, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
   for (int attempt = 1; attempt <= 3; attempt++) {
     try {
       transactions.executeWithoutResult(status -> {
         if (event.messageId() == null || event.messageId().isBlank()) throw new IllegalArgumentException("messageId is required");
         if (processed.existsByMessageId(event.messageId())) return;
         Order order = orders.findById(event.businessId()).orElseThrow(() -> new IllegalArgumentException("order not found"));
         if (order.getVoucherCode() == null) order.setVoucherCode(newVoucherCode());
         var user = users.findById(order.getUserId()).orElseThrow(() -> new IllegalArgumentException("user not found"));
         if (user.getInviterId() != null)
           invitations.processInvitationReward(user.getInviterId(), user.getId(), order.getFinalPrice());
         if (!notifications.existsByOrderId(order.getId())) notifications.save(new OrderNotification(
                 order.getId(), order.getUserId(), "订单 " + order.getBusinessNo() + " 已创建，券码已生成"));
         processed.save(new ProcessedMessage(event.messageId()));
       });
       channel.basicAck(deliveryTag, false);
       return;
     } catch (RuntimeException ex) {
       if (attempt == 3) { log.warn("message sent to dead letter queue after retries: {}", ex.toString()); channel.basicNack(deliveryTag, false, false); return; }
       try { Thread.sleep(100L * (1L << (attempt - 1))); }
       catch (InterruptedException interrupted) { Thread.currentThread().interrupt(); channel.basicNack(deliveryTag, false, false); return; }
     }
   }
 }

 private String newVoucherCode() {
   for (int attempt = 0; attempt < 5; attempt++) {
     StringBuilder value = new StringBuilder(16);
     for (int i = 0; i < 16; i++) value.append(RANDOM.nextInt(10));
     String code = value.toString();
     if (!orders.existsByVoucherCode(code)) return code;
   }
   throw new IllegalStateException("unable to generate unique voucher code");
 }
}
