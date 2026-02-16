package vn.tt.practice.apigateway.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Chuẩn response JSON thống nhất cho toàn hệ thống.
 * Gateway và các API con (user, product, order...) nên trả về cùng format này để client
 * gọi qua gateway nhận response đồng nhất: success, message, data, timestamp (và code/path khi lỗi).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;

    /**
     * Thời điểm trả về (ISO-8601).
     */
    private String timestamp;

    /**
     * Mã lỗi HTTP hoặc mã nghiệp vụ (ví dụ: UNAUTHORIZED, RATE_LIMIT_EXCEEDED).
     */
    private String code;

    /**
     * Đường dẫn request gây lỗi (hữu ích cho client log/debug).
     */
    private String path;

    /**
     * Request ID từ header X-Request-Id (nếu có).
     */
    private String requestId;

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message("Success")
                .data(data)
                .timestamp(Instant.now().toString())
                .build();
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message != null ? message : "Success")
                .data(data)
                .timestamp(Instant.now().toString())
                .build();
    }

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .timestamp(Instant.now().toString())
                .build();
    }

    public static <T> ApiResponse<T> error(String message, String code) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .code(code)
                .timestamp(Instant.now().toString())
                .build();
    }

    public static <T> ApiResponse<T> error(String message, String code, String path, String requestId) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .code(code)
                .path(path)
                .requestId(requestId)
                .timestamp(Instant.now().toString())
                .build();
    }
}
