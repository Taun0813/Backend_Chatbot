package vn.tt.practice.recommendationservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String RECOMMENDATION_EXCHANGE = "recommendation.exchange";
    public static final String ORDER_EXCHANGE = "order.exchange";
    public static final String ORDER_COMPLETED_QUEUE = "order.completed";
    public static final String PRODUCT_VIEWED_QUEUE = "product.viewed";

    @Bean
    public TopicExchange recommendationExchange() {
        return new TopicExchange(RECOMMENDATION_EXCHANGE);
    }

    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(ORDER_EXCHANGE);
    }

    @Bean
    public Queue orderCompletedQueue() {
        return new Queue(ORDER_COMPLETED_QUEUE, true);
    }

    @Bean
    public Queue productViewedQueue() {
        return new Queue(PRODUCT_VIEWED_QUEUE, true);
    }

    @Bean
    public Binding orderCompletedBinding() {
        return BindingBuilder
                .bind(orderCompletedQueue())
                .to(orderExchange())
                .with("order.completed");
    }

    @Bean
    public Binding productViewedBinding() {
        return BindingBuilder
                .bind(productViewedQueue())
                .to(recommendationExchange())
                .with("product.viewed");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
