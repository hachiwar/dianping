package org.com.dianping.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.*;

@Configuration
public class RabbitConfig {
    static final String EXCHANGE = "dianping.events", QUEUE = "order.created", DLQ = "order.created.dlq";
    @Bean DirectExchange eventsExchange() { return new DirectExchange(EXCHANGE); }
    @Bean Queue orderQueue() { return QueueBuilder.durable(QUEUE).withArgument("x-dead-letter-exchange", EXCHANGE).withArgument("x-dead-letter-routing-key", "order.dead").build(); }
    @Bean Queue deadLetterQueue() { return QueueBuilder.durable(DLQ).build(); }
    @Bean Binding orderBinding() { return BindingBuilder.bind(orderQueue()).to(eventsExchange()).with("order.created"); }
    @Bean Binding deadLetterBinding() { return BindingBuilder.bind(deadLetterQueue()).to(eventsExchange()).with("order.dead"); }
}
