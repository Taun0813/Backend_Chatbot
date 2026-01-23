package vn.tt.practice.userservice.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StandardResponse<T> {
    private T data;
    private Meta meta;
    private Error error;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Meta {
        private String status;
        private String message;
        private long timestamp;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Error {
        private String code;
        private String message;
        private Object details;
    }

    public static <T> StandardResponse<T> success(T data, String message) {
        return StandardResponse.<T>builder()
                .data(data)
                .meta(Meta.builder()
                        .status("SUCCESS")
                        .message(message)
                        .timestamp(System.currentTimeMillis())
                        .build())
                .build();
    }

    public static <T> StandardResponse<T> error(String code, String message, Object details) {
        return StandardResponse.<T>builder()
                .meta(Meta.builder()
                        .status("ERROR")
                        .message(message)
                        .timestamp(System.currentTimeMillis())
                        .build())
                .error(Error.builder()
                        .code(code)
                        .message(message)
                        .details(details)
                        .build())
                .build();
    }
}
