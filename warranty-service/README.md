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
