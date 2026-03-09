package vn.tt.practice.orderservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String ORDER_EXCHANGE = "order.exchange";
    public static final String INVENTORY_EXCHANGE = "inventory.exchange";
    public static final String PAYMENT_EXCHANGE = "payment.exchange";

    public static final String PAYMENT_COMPLETED_QUEUE = "payment.completed.queue";
    public static final String PAYMENT_FAILED_QUEUE = "payment.failed.queue";

    public static final String INVENTORY_RESERVED_QUEUE = "inventory.reserved.queue";
    public static final String INVENTORY_RESERVATION_FAILED_QUEUE = "inventory.reservation.failed.queue";

    @Bean
    public TopicExchange orderExchange() { return new TopicExchange(ORDER_EXCHANGE, true, false); }

    @Bean
    public TopicExchange inventoryExchange() { return new TopicExchange(INVENTORY_EXCHANGE, true, false); }

    @Bean
    public TopicExchange paymentExchange() { return new TopicExchange(PAYMENT_EXCHANGE, true, false); }

    @Bean
    public Queue inventoryReservedQueue() {
        return QueueBuilder.durable(INVENTORY_RESERVED_QUEUE).build();
    }

    @Bean
    public Queue inventoryReservationFailedQueue() {
        return QueueBuilder.durable(INVENTORY_RESERVATION_FAILED_QUEUE).build();
    }

    @Bean
    public Binding inventoryReservedBinding() {
        return BindingBuilder.bind(inventoryReservedQueue())
                .to(inventoryExchange())
                .with("inventory.reserved");
    }

    @Bean
    public Binding inventoryReservationFailedBinding() {
        return BindingBuilder.bind(inventoryReservationFailedQueue())
                .to(inventoryExchange())
                .with("inventory.reservation.failed");
    }

    @Bean
    public MessageConverter jsonMessageConverter() { return new Jackson2JsonMessageConverter(); }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory cf) {
        RabbitTemplate template = new RabbitTemplate(cf);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
