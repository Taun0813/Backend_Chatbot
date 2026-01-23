package vn.tt.practice.apigateway.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
@Slf4j
@RequiredArgsConstructor
public class RateLimitFilter implements GlobalFilter, Ordered {

    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private static final int REQUEST_LIMIT = 100;
    private static final Duration TIME_WINDOW = Duration.ofMinutes(1);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Identify the client (by User ID from header if available, or IP)
        String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
        String clientIp = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
        
        String key = "rate_limit:" + (userId != null ? userId : clientIp);

        return redisTemplate.opsForValue().increment(key)
                .flatMap(count -> {
                    if (count == 1) {
                        return redisTemplate.expire(key, TIME_WINDOW).thenReturn(count);
                    }
                    return Mono.just(count);
                })
                .flatMap(count -> {
                    if (count > REQUEST_LIMIT) {
                        log.warn("Rate limit exceeded for client: {}", key);
                        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                        return exchange.getResponse().setComplete();
                    }
                    return chain.filter(exchange);
                });
    }

    @Override
    public int getOrder() {
        return -1; // Run before other filters (but after Auth filter if I set Auth order < -1)
        // Wait, Auth filter must run BEFORE Rate Limit if we want to use User ID.
        // So Auth Filter should be -2, Rate Limit -1.
    }
}
