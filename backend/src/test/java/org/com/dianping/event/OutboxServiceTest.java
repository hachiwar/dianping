package org.com.dianping.event;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.time.Instant;
import java.util.Map;
import org.com.dianping.repository.OutboxEventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

class OutboxServiceTest {
    @Test void deletesOutboxEventOnlyAfterBrokerConfirmation() {
        OutboxEventRepository events = mock(OutboxEventRepository.class); RabbitTemplate rabbit = mock(RabbitTemplate.class);
        doAnswer(invocation -> { invocation.<CorrelationData>getArgument(3).getFuture().complete(new CorrelationData.Confirm(true, null)); return null; })
                .when(rabbit).convertAndSend(anyString(), anyString(), any(Object.class), any(CorrelationData.class));
        new OutboxService(events, rabbit).publish(new OrderCreated("event-1", "OrderCreated", 1L, Instant.now(), Map.of()));
        verify(events).deleteByMessageId("event-1");
    }
}
