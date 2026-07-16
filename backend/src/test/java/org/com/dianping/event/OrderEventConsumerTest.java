package org.com.dianping.event;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.time.Instant;
import java.util.Map;
import org.com.dianping.repository.ProcessedMessageRepository;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;
import com.rabbitmq.client.Channel;

class OrderEventConsumerTest {
    @Test void acknowledgesOnlyAfterPersistingMessageId() throws Exception {
        ProcessedMessageRepository messages = mock(ProcessedMessageRepository.class); PlatformTransactionManager transactions = transactionManager(); Channel channel = mock(Channel.class);
        when(messages.existsByMessageId("event-1")).thenReturn(false);
        new OrderEventConsumer(messages, transactions).consume(new OrderCreated("event-1", "OrderCreated", 1L, Instant.now(), Map.of()), channel, 3L);
        verify(messages).save(any()); verify(channel).basicAck(3L, false);
    }
    @Test void deadLettersInvalidMessageAfterBoundedRetries() throws Exception {
        Channel channel = mock(Channel.class);
        new OrderEventConsumer(mock(ProcessedMessageRepository.class), transactionManager()).consume(new OrderCreated(null, "OrderCreated", 1L, Instant.now(), Map.of()), channel, 4L);
        verify(channel).basicNack(4L, false, false);
    }
    private PlatformTransactionManager transactionManager() {
        PlatformTransactionManager manager = mock(PlatformTransactionManager.class);
        when(manager.getTransaction(any())).thenAnswer(ignored -> new SimpleTransactionStatus());
        return manager;
    }
}
