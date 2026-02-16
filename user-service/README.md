## User Service

### Chức năng

- Quản lý người dùng:
  - Đăng ký, đăng nhập.
  - Hồ sơ người dùng, địa chỉ.
  - Phân quyền (ROLE_USER, ROLE_ADMIN, ROLE_SUPER_ADMIN).
- Quản lý refresh token (nếu cấu hình).

### Cổng

- Mặc định: `8081`

### Endpoint chính (tham khảo)

- Auth:
  - `POST /auth/register`
  - `POST /auth/login`
  - `POST /auth/refresh`
  - `POST /auth/refresh-token`
- User:
  - `GET /users/me` – thông tin user hiện tại.
  - `GET /users` – danh sách user (ADMIN).
  - `GET /users/{id}` – chi tiết user (ADMIN).
  - `PUT /users/{id}/role` – đổi role (SUPER_ADMIN).
  - `DELETE /users/{id}` – xoá user (SUPER_ADMIN).

### Bảo mật

- SecurityConfig trong `user-service` đơn giản:
  - Cho phép `/auth/**`, `/users/**`, swagger, actuator.
  - Không decode JWT; không dùng `@PreAuthorize` hay filter auth riêng.
- Kiểm tra quyền bằng header `X-User-Roles` ngay trong controller:
  - ADMIN / SUPER_ADMIN mới truy cập được các endpoint quản trị.

### Persistence

- PostgreSQL.
- Entity chính:
  - `User`, `UserProfile`, `Address` (kế thừa `BaseEntity` với `id`, `createdAt`, `updatedAt`, ...).
- Migrations Flyway trong `src/main/resources/db/migration/V1__init_schema.sql`.

### Chạy service

```bash
mvn spring-boot:run
```

# User Service

## Overview
User management service handling authentication, registration, profile management, and role-based access control.

## Key Features
- **Authentication**: JWT-based login, registration, and token refresh.
- **Microservice Integration**: Registers with Eureka, communicates via RabbitMQ (Event-driven).
- **Security**: Role-based access control (USER, ADMIN, SUPER_ADMIN).
- **Database**: PostgreSQL for user data and profiles.

## API Documentation
Swagger UI: `http://localhost:8081/swagger-ui.html`

## Running Locally
```bash
mvn spring-boot:run
```

## Docker
```bash
docker build -t user-service .
docker run -p 8081:8081 user-service
```
