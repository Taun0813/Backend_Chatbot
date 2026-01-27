package vn.tt.practice.apigateway.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;

@Slf4j
public class ApiResponseUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static Mono<Void> sendErrorResponse(
            ServerWebExchange exchange,
            HttpStatus status,
            String code,
            String message,
            List<String> details
    ) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String requestId = exchange.getRequest().getHeaders().getFirst("X-Request-Id");

        ErrorResponse errorResponse = ErrorResponse.builder()
                .error(ErrorDetail.builder()
                        .code(code)
                        .message(message)
                        .details(details)
                        .build())
                .meta(MetaInfo.builder()
                        .traceId(requestId)
                        .timestamp(Instant.now().toString())
                        .build())
                .build();

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(errorResponse);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("Error writing response", e);
            return response.setComplete();
        }
    }

    public static Mono<Void> sendUnauthorizedResponse(ServerWebExchange exchange, String message) {
        return sendErrorResponse(
                exchange,
                HttpStatus.UNAUTHORIZED,
                "UNAUTHORIZED",
                message,
                List.of("Please provide a valid Bearer token")
        );
    }

    public static Mono<Void> sendForbiddenResponse(ServerWebExchange exchange, String message) {
        return sendErrorResponse(
                exchange,
                HttpStatus.FORBIDDEN,
                "FORBIDDEN",
                message,
                List.of("You don't have permission to access this resource")
        );
    }

    public static Mono<Void> sendTooManyRequestsResponse(ServerWebExchange exchange) {
        return sendErrorResponse(
                exchange,
                HttpStatus.TOO_MANY_REQUESTS,
                "RATE_LIMIT_EXCEEDED",
                "Too many requests",
                List.of("Rate limit exceeded. Please try again later.")
        );
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorResponse {
        private ErrorDetail error;
        private MetaInfo meta;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorDetail {
        private String code;
        private String message;
        private List<String> details;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetaInfo {
        private String traceId;
        private String timestamp;
    }
}
