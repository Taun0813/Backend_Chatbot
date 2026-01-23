package vn.tt.practice.apigateway.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import vn.tt.practice.apigateway.util.ApiResponseUtil;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitFilter implements GlobalFilter, Ordered {

    private final ReactiveStringRedisTemplate redisTemplate;

    private static final int MAX_REQUESTS_PER_MINUTE = 100;
    private static final Duration WINDOW_DURATION = Duration.ofMinutes(1);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Get userId from header (set by JWT filter) or use IP address
        String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
        String clientIdentifier = userId != null ? "user:" + userId : "ip:" + getClientIp(exchange);

        String redisKey = "rate_limit:" + clientIdentifier;

        return redisTemplate.opsForValue()
                .increment(redisKey)
                .flatMap(count -> {
                    if (count == null) {
                        count = 0L;
                    }

                    // Set expiration on first request
                    if (count == 1) {
                        redisTemplate.expire(redisKey, WINDOW_DURATION).subscribe();
                    }

                    if (count > MAX_REQUESTS_PER_MINUTE) {
                        log.warn("Rate limit exceeded for: {} (count: {})", clientIdentifier, count);
                        return ApiResponseUtil.sendTooManyRequestsResponse(exchange);
                    }

                    log.debug("Rate limit check passed for: {} ({}/{})", 
                            clientIdentifier, count, MAX_REQUESTS_PER_MINUTE);

                    // Add rate limit headers
                    exchange.getResponse().getHeaders()
                            .add("X-RateLimit-Limit", String.valueOf(MAX_REQUESTS_PER_MINUTE));
                    exchange.getResponse().getHeaders()
                            .add("X-RateLimit-Remaining", String.valueOf(MAX_REQUESTS_PER_MINUTE - count));

                    return chain.filter(exchange);
                })
                .onErrorResume(e -> {
                    log.error("Rate limit check failed: {}", e.getMessage());
                    // Continue on Redis error (fail open)
                    return chain.filter(exchange);
                });
    }

    private String getClientIp(ServerWebExchange exchange) {
        String ip = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = exchange.getRequest().getRemoteAddress() != null
                    ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                    : "unknown";
        }
        return ip;
    }

    @Override
    public int getOrder() {
        return -90; // Run after JWT filter
    }
}
