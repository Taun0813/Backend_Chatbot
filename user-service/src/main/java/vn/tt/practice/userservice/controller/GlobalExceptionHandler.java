package vn.tt.practice.userservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import vn.tt.practice.userservice.dto.ApiResponse;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("code", "INTERNAL_SERVER_ERROR");
        error.put("message", ex.getMessage());
        error.put("details", List.of());

        Map<String, Object> meta = new HashMap<>();
        meta.put("traceId", MDC.get("traceId"));
        meta.put("timestamp", LocalDateTime.now().toString());

        Map<String, Object> response = new HashMap<>();
        response.put("error", error);
        response.put("meta", meta);

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());

        Map<String, Object> error = new HashMap<>();
        error.put("code", "VALIDATION_ERROR");
        error.put("message", "Validation failed");
        error.put("details", details);

        Map<String, Object> meta = new HashMap<>();
        meta.put("traceId", MDC.get("traceId"));
        meta.put("timestamp", LocalDateTime.now().toString());

        Map<String, Object> response = new HashMap<>();
        response.put("error", error);
        response.put("meta", meta);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
