## Gateway Service

### Chức năng

- Là entrypoint duy nhất cho client (Web/Mobile).
- Route request tới các microservice theo path.
- Validate JWT và đính kèm thông tin người dùng vào header:
  - `X-User-Id`
  - `X-User-Roles`

### Công nghệ

- Spring Boot 3
- Spring Cloud Gateway
- Spring Security (JWT)
- Eureka Client

### Cổng

- Mặc định: `8181`

### Bảo mật

- Nhận JWT từ header `Authorization: Bearer <token>`.
- Validate token (chữ ký, hạn, issuer, audience).
- Nếu hợp lệ:
  - Trích `userId`, `roles` từ JWT.
  - Gắn vào header gửi xuống các service:
    - `X-User-Id: <userId>`
    - `X-User-Roles: ROLE_USER,ROLE_ADMIN,...`
- Các route public như `/api/auth/**`, swagger, actuator được cấu hình bypass JWT.

### Cấu hình route (ví dụ)

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/users/**, /api/auth/** 
        - id: product-service
          uri: lb://product-service
          predicates:
            - Path=/api/products/**, /api/categories/**
```

### Chạy service

```bash
mvn spring-boot:run
```

