package org.com.dianping.event;

import java.util.Properties;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class DeadLetterService {
    private final RabbitAdmin admin;
    private final RabbitTemplate rabbit;
    public DeadLetterService(RabbitAdmin admin, RabbitTemplate rabbit) { this.admin = admin; this.rabbit = rabbit; }
    public int pending() { try { Properties properties = admin.getQueueProperties("order.created.dlq"); return properties == null ? 0 : (Integer) properties.get(RabbitAdmin.QUEUE_MESSAGE_COUNT); } catch (RuntimeException e) { return -1; } }
    public boolean replayOne() {
        try { Message message = rabbit.receive("order.created.dlq"); if (message == null) return false; rabbit.send("dianping.events", "order.created", message); return true; } catch (RuntimeException e) { throw new IllegalStateException("死信补偿失败", e); }
    }
}
