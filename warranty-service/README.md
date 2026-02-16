## Warranty Service

### Chức năng

- Quản lý bảo hành sản phẩm.
- Tự động tạo warranty khi đơn hàng được thanh toán.
- Quản lý và xử lý yêu cầu bảo hành (warranty claims).

### Cổng

- Mặc định: `8088`

### Bảng chính

- `warranties`:
  - `order_id`, `product_id`, `user_id`
  - `warranty_number` (unique)
  - `start_date`, `end_date`
  - `warranty_period_months`
  - `status` (ACTIVE, EXPIRED, VOID, CLAIMED)
  - timestamps
- `warranty_claims`:
  - `warranty_id`, `user_id`
  - `claim_number` (unique)
  - `description`, `status`, `resolution`
  - `submitted_at`, `resolved_at`, timestamps

### Enum

- `WarrantyStatus`:
  - ACTIVE, EXPIRED, VOID, CLAIMED
- `ClaimStatus`:
  - PENDING, APPROVED, REJECTED, PROCESSING, COMPLETED

### Business logic

1. Khi order được thanh toán (OrderPaidEvent / status PAID):
   - Tự động tạo `Warranty` với:
     - Thời gian bảo hành mặc định 12 tháng.
     - `warrantyNumber = "WTY-{timestamp}-{random}"`.
2. Khi user submit claim:
   - Kiểm tra:
     - Warranty thuộc về user.
     - Status warranty còn ACTIVE.
     - Ngày hiện tại không vượt quá `end_date`.
   - Nếu hết hạn:
     - Set status EXPIRED và ném `WarrantyExpiredException`.
3. Khi cập nhật claim status:
   - Ghi `resolution`, cập nhật `resolved_at` nếu COMPLETED/REJECTED.
   - Publish event thông báo thay đổi để gửi notification hoặc log.

### Endpoint chính

- Warranty:
  - `GET /warranties/order/{orderId}` – danh sách warranty theo order.
  - `GET /warranties/{id}` – chi tiết 1 warranty.
  - `GET /warranties/user/me` – tất cả warranty của user hiện tại.
- Warranty claim:
  - `POST /warranties/claims` – submit claim mới (body `WarrantyClaimRequest`).
  - `GET /warranties/claims/{id}` – chi tiết claim.
  - `PUT /warranties/claims/{id}/status` – cập nhật trạng thái claim (ADMIN).
  - `GET /warranties/claims` – danh sách claim (ADMIN, paginated).

### Sự kiện

- `WarrantyEventListener`:
  - Lắng nghe queue `order.paid` từ `order.exchange`.
  - Nhận `OrderEvent` (khớp với `order-service`) và tạo warranty cho từng item trong order.

### Bảo mật

- SecurityConfig:
  - Cho phép `/warranties/**`, swagger, actuator.
- Controller:
  - `PUT /warranties/claims/{id}/status` và `GET /warranties/claims` yêu cầu ADMIN/SUPER_ADMIN, check qua `X-User-Roles`.
  - `GET /warranties/user/me` lấy `userId` từ header `X-User-Id`.

### Chạy service

```bash
mvn spring-boot:run
```

## Warranty Service (Port 8088)

**Chức năng**: Quản lý bảo hành cho sản phẩm sau khi đơn hàng được thanh toán thành công:
- Tự động tạo bản ghi bảo hành khi order được đánh dấu "PAID"
- Cho phép người dùng xem thông tin bảo hành và gửi yêu cầu bảo hành (claim)
- Quản lý trạng thái claim (PENDING → APPROVED/REJECTED → PROCESSING → COMPLETED)
- Kiểm tra tính hợp lệ của bảo hành trước khi cho phép claim

### Tech
- Spring Boot 3.5.7, Java 21
- Spring Data JPA (PostgreSQL) + Flyway
- RabbitMQ (events)
- Redis (caching)
- Eureka Client
- Spring Boot Actuator
- SpringDoc OpenAPI (Swagger UI)

### Cấu trúc chính
- `WarrantyServiceApplication` – main class với `@EnableDiscoveryClient`
- `controller/WarrantyController` – REST endpoints với Swagger annotations
- `entity/Warranty`, `WarrantyClaim` – entities với đầy đủ fields
- `enums/WarrantyStatus` – ACTIVE, EXPIRED, VOID, CLAIMED
- `enums/ClaimStatus` – PENDING, APPROVED, REJECTED, PROCESSING, COMPLETED
- `repository/*` – `WarrantyRepository`, `WarrantyClaimRepository`
- `service/WarrantyService` (interface), `WarrantyServiceImpl` – business logic
- `event/WarrantyEventListener`, `WarrantyEventPublisher` – RabbitMQ events
- `dto/*` – `WarrantyDTO`, `WarrantyClaimDTO`, `WarrantyClaimRequest`
- `exception/*` – `GlobalExceptionHandler`, `WarrantyNotFoundException`, `WarrantyExpiredException`
- `config/*` – `SecurityConfig`, `RabbitMQConfig`, `OpenApiConfig`
- `resources/db/migration/V1__init_schema.sql` – database schema

### Business Logic
- **Auto-create warranty**: Tự động tạo warranty khi order được đánh dấu PAID (default 12 months)
- **Generate warranty number**: Format `WTY-{timestamp}-{random}`
- **Validate warranty**: Kiểm tra warranty còn active và chưa hết hạn trước khi cho phép claim
- **Claim management**: Submit claim, update status, track resolution

### Endpoints chính
Public:
- `GET /warranties/order/{orderId}` – Lấy warranties theo order ID
- `GET /warranties/{id}` – Chi tiết warranty
- `GET /warranties/user/me` – Warranties của user hiện tại
- `POST /warranties/claims` – Gửi warranty claim
- `GET /warranties/claims/{id}` – Chi tiết claim

Admin (ROLE_ADMIN, ROLE_SUPER_ADMIN):
- `PUT /warranties/claims/{id}/status` – Cập nhật trạng thái claim
- `GET /warranties/claims` – Tất cả claims (phân trang)

### Event Flow (RabbitMQ)
- **OrderPaidEvent** → WarrantyService (createWarranty cho mỗi product trong order)
- **ClaimStatusChangedEvent** → Notification service (gửi thông báo cho user)

### Cấu hình
#### `application.yml` (local)
- Port: `8088`
- DB: `jdbc:postgresql://localhost:5433/ai_agent`
- Redis: `localhost:6379`
- RabbitMQ: `localhost:5672`
- Eureka: `http://localhost:8761/eureka/`
- Actuator: health, metrics, prometheus

#### `application-docker.yml`
- DB: `postgresql:5432/ai_agent`
- Redis: `redis:6379`
- RabbitMQ: `rabbitmq:5672`
- Eureka: `http://ai-agent-discovery:8761/eureka/`

### Chạy local
```bash
cd warranty-service
mvn clean package
mvn spring-boot:run
```

Swagger UI:  
`http://localhost:8088/swagger-ui.html`

### Docker
```bash
cd warranty-service
mvn clean package -DskipTests
docker build -t warranty-service:latest .
docker run -p 8088:8088 warranty-service:latest
```

Warranty Service được route qua API Gateway với prefix:
- `/api/warranties/**`
