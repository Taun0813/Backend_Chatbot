package vn.tt.practice.cartservice.config;

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
    public static final String DOMAIN_EXCHANGE = "domain-events-exchange";

    public static final String ORDER_CREATED_QUEUE = "cart.order-created.queue";
    public static final String PRODUCT_DELETED_QUEUE = "cart.product-deleted.queue";

    public static final String ORDER_CREATED_ROUTING_KEY = "order.created";
    public static final String PRODUCT_DELETED_ROUTING_KEY = "product.deleted";

    @Bean
    TopicExchange orderExchange() {
        return new TopicExchange(ORDER_EXCHANGE, true, false);
    }

    @Bean
    TopicExchange domainExchange() {
        return new TopicExchange(DOMAIN_EXCHANGE, true, false);
    }

    @Bean
    Queue orderCreatedQueue() {
        return QueueBuilder.durable(ORDER_CREATED_QUEUE).build();
    }

    @Bean
    Queue productDeletedQueue() {
        return QueueBuilder.durable(PRODUCT_DELETED_QUEUE).build();
    }

    @Bean
    Binding orderCreatedBinding() {
        return BindingBuilder
                .bind(orderCreatedQueue())
                .to(orderExchange())
                .with(ORDER_CREATED_ROUTING_KEY);
    }

    @Bean
    Binding productDeletedBinding() {
        return BindingBuilder
                .bind(productDeletedQueue())
                .to(domainExchange())
                .with(PRODUCT_DELETED_ROUTING_KEY);
    }

    @Bean
    MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}

