package org.com.dianping.event;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import org.com.dianping.entity.Order;
import org.com.dianping.entity.User;
import org.com.dianping.repository.OrderNotificationRepository;
import org.com.dianping.repository.OrderRepository;
import org.com.dianping.repository.ProcessedMessageRepository;
import org.com.dianping.repository.UserRepository;
import org.com.dianping.service.InvitationService;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;
import com.rabbitmq.client.Channel;

class OrderEventConsumerTest {
    @Test void acknowledgesOnlyAfterPersistingMessageId() throws Exception {
        ProcessedMessageRepository messages = mock(ProcessedMessageRepository.class); Channel channel = mock(Channel.class);
        OrderRepository orders = mock(OrderRepository.class); UserRepository users = mock(UserRepository.class);
        InvitationService invitations = mock(InvitationService.class); OrderNotificationRepository notifications = mock(OrderNotificationRepository.class);
        Order order = mock(Order.class); User user = mock(User.class);
        when(messages.existsByMessageId("event-1")).thenReturn(false);
        when(orders.findById(1L)).thenReturn(Optional.of(order)); when(orders.existsByVoucherCode(anyString())).thenReturn(false);
        when(order.getId()).thenReturn(1L); when(order.getUserId()).thenReturn(2L); when(order.getBusinessNo()).thenReturn("DP1"); when(order.getFinalPrice()).thenReturn(new BigDecimal("20.00"));
        when(users.findById(2L)).thenReturn(Optional.of(user)); when(user.getId()).thenReturn(2L); when(user.getInviterId()).thenReturn(9L);
        new OrderEventConsumer(messages, orders, users, invitations, notifications, transactionManager())
                .consume(new OrderCreated("event-1", "OrderCreated", 1L, Instant.now(), Map.of()), channel, 3L);
        verify(order).setVoucherCode(argThat(code -> code.matches("\\d{16}")));
        verify(invitations).processInvitationReward(9L, 2L, new BigDecimal("20.00"));
        verify(notifications).save(any()); verify(messages).save(any()); verify(channel).basicAck(3L, false);
    }
    @Test void deadLettersInvalidMessageAfterBoundedRetries() throws Exception {
        Channel channel = mock(Channel.class);
        new OrderEventConsumer(mock(ProcessedMessageRepository.class), mock(OrderRepository.class),
                mock(UserRepository.class), mock(InvitationService.class), mock(OrderNotificationRepository.class), transactionManager())
                .consume(new OrderCreated(null, "OrderCreated", 1L, Instant.now(), Map.of()), channel, 4L);
        verify(channel).basicNack(4L, false, false);
    }
    @Test void duplicateMessageDoesNotRepeatPostProcessing() throws Exception {
        ProcessedMessageRepository messages = mock(ProcessedMessageRepository.class); OrderRepository orders = mock(OrderRepository.class);
        UserRepository users = mock(UserRepository.class); InvitationService invitations = mock(InvitationService.class);
        OrderNotificationRepository notifications = mock(OrderNotificationRepository.class); Channel channel = mock(Channel.class);
        when(messages.existsByMessageId("event-1")).thenReturn(true);

        new OrderEventConsumer(messages, orders, users, invitations, notifications, transactionManager())
                .consume(new OrderCreated("event-1", "OrderCreated", 1L, Instant.now(), Map.of()), channel, 5L);

        verifyNoInteractions(orders, users, invitations, notifications);
        verify(channel).basicAck(5L, false);
    }
    private PlatformTransactionManager transactionManager() {
        PlatformTransactionManager manager = mock(PlatformTransactionManager.class);
        when(manager.getTransaction(any())).thenAnswer(ignored -> new SimpleTransactionStatus());
        return manager;
    }
}
