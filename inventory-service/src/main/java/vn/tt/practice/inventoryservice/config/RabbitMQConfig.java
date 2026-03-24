package vn.tt.practice.inventoryservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Exchanges
    public static final String ORDER_EXCHANGE = "order.exchange";
    public static final String INVENTORY_EXCHANGE = "inventory.exchange";
    public static final String PAYMENT_EXCHANGE = "payment.exchange";

    // Queues - Inventory Service listens to
    public static final String ORDER_CREATED_QUEUE = "order.created.queue";
    public static final String PAYMENT_COMPLETED_QUEUE = "payment.completed.queue";
    public static final String PAYMENT_FAILED_QUEUE = "payment.failed.queue";
    public static final String ORDER_CANCELLED_QUEUE = "order.cancelled.queue";

    // Routing Keys for publishing
    public static final String INVENTORY_RESERVED_ROUTING_KEY = "inventory.reserved";
    public static final String INVENTORY_RESERVATION_FAILED_ROUTING_KEY = "inventory.reservation.failed";
    public static final String STOCK_UPDATED_ROUTING_KEY = "stock.updated";

    // Order Exchange (for listening)
    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(ORDER_EXCHANGE, true, false);
    }

    // Payment Exchange (for listening)
    @Bean
    public TopicExchange paymentExchange() {
        return new TopicExchange(PAYMENT_EXCHANGE, true, false);
    }

    // Inventory Exchange (for publishing)
    @Bean
    public TopicExchange inventoryExchange() {
        return new TopicExchange(INVENTORY_EXCHANGE, true, false);
    }

    // Queues
    @Bean
    public Queue orderCreatedQueue() {
        return QueueBuilder.durable(ORDER_CREATED_QUEUE).build();
    }

    @Bean
    public Queue paymentCompletedQueue() {
        return QueueBuilder.durable(PAYMENT_COMPLETED_QUEUE).build();
    }

    @Bean
    public Queue paymentFailedQueue() {
        return QueueBuilder.durable(PAYMENT_FAILED_QUEUE).build();
    }

    @Bean
    public Queue orderCancelledQueue() {
        return QueueBuilder.durable(ORDER_CANCELLED_QUEUE).build();
    }

    // Bindings
    @Bean
    public Binding orderCreatedBinding() {
        return BindingBuilder
                .bind(orderCreatedQueue())
                .to(orderExchange())
                .with("order.created");
    }

    @Bean
    public Binding paymentCompletedBinding() {
        return BindingBuilder
                .bind(paymentCompletedQueue())
                .to(paymentExchange())
                .with("payment.completed");
    }

    @Bean
    public Binding paymentFailedBinding() {
        return BindingBuilder
                .bind(paymentFailedQueue())
                .to(paymentExchange())
                .with("payment.failed");
    }

    @Bean
    public Binding orderCancelledBinding() {
        return BindingBuilder
                .bind(orderCancelledQueue())
                .to(orderExchange())
                .with("order.cancelled");
    }

    // Message Converter
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // RabbitTemplate
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
