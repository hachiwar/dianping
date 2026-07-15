package org.com.dianping.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.support.converter.*;

@Configuration
public class RabbitConfig {
    static final String EXCHANGE = "dianping.events", QUEUE = "order.created", DLQ = "order.created.dlq";
    @Bean DirectExchange eventsExchange() { return new DirectExchange(EXCHANGE); }
    @Bean Queue orderQueue() { return QueueBuilder.durable(QUEUE).withArgument("x-dead-letter-exchange", EXCHANGE).withArgument("x-dead-letter-routing-key", "order.dead").build(); }
    @Bean Queue deadLetterQueue() { return QueueBuilder.durable(DLQ).build(); }
    @Bean Binding orderBinding() { return BindingBuilder.bind(orderQueue()).to(eventsExchange()).with("order.created"); }
    @Bean Binding deadLetterBinding() { return BindingBuilder.bind(deadLetterQueue()).to(eventsExchange()).with("order.dead"); }
    @Bean MessageConverter messageConverter() { return new Jackson2JsonMessageConverter(); }
    @Bean SimpleRabbitListenerContainerFactory manualRabbitListenerFactory(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory); factory.setAcknowledgeMode(AcknowledgeMode.MANUAL); factory.setMessageConverter(messageConverter);
        factory.setAdviceChain(RetryInterceptorBuilder.stateless().maxAttempts(3).recoverer(new RejectAndDontRequeueRecoverer()).build());
        return factory;
    }
}
