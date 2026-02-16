package vn.tt.practice.apigateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import vn.tt.practice.apigateway.dto.ApiResponse;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Component
@Slf4j
@RequiredArgsConstructor
public class RateLimitFilter implements GlobalFilter, Ordered {

    private static final int REQUEST_LIMIT = 100;
    private static final Duration TIME_WINDOW = Duration.ofMinutes(1);
    private static final String CODE_RATE_LIMIT = "RATE_LIMIT_EXCEEDED";
    private static final String REQUEST_ID_HEADER = "X-Request-Id";

    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
        String clientIp = exchange.getRequest().getRemoteAddress() != null
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                : "unknown";

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
                        return writeApiResponse(exchange, HttpStatus.TOO_MANY_REQUESTS,
                                ApiResponse.error("Quá nhiều request. Vui lòng thử lại sau.", CODE_RATE_LIMIT,
                                        exchange.getRequest().getPath().value(),
                                        exchange.getRequest().getHeaders().getFirst(REQUEST_ID_HEADER)));
                    }
                    return chain.filter(exchange);
                });
    }

    private Mono<Void> writeApiResponse(ServerWebExchange exchange, HttpStatus status, ApiResponse<?> body) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(body);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize ApiResponse", e);
            bytes = ("{\"success\":false,\"message\":\"" + (body.getMessage() != null ? body.getMessage().replace("\"", "\\\"") : "Error") + "\"}").getBytes(StandardCharsets.UTF_8);
        }
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -1; // Run before other filters (but after Auth filter if I set Auth order < -1)
        // Wait, Auth filter must run BEFORE Rate Limit if we want to use User ID.
        // So Auth Filter should be -2, Rate Limit -1.
    }
}