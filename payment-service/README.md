## Payment Service (Port 8085)

**Chức năng**: Xử lý thanh toán cho đơn hàng:
- Nhận yêu cầu thanh toán cho một `orderId`
- Gọi sang cổng thanh toán (mock implementation với 80% success rate)
- Xử lý callback từ payment gateway
- Hỗ trợ refund payment
- Gửi sự kiện `PaymentCompleted` / `PaymentFailed` / `PaymentRefunded` cho các service khác

### Tech
- Spring Boot 3.5.7, Java 21
- Spring Data JPA (PostgreSQL) + Flyway
- RabbitMQ (events)
- Redis (caching)
- Eureka Client
- Spring Boot Actuator
- SpringDoc OpenAPI (Swagger UI)

### Cấu trúc chính
- `PaymentServiceApplication` – main class với `@EnableDiscoveryClient`
- `controller/PaymentController` – REST endpoints với Swagger annotations
- `entity/Payment` – bảng `payments` lưu thông tin thanh toán
- `enums/PaymentStatus` – PENDING, PROCESSING, COMPLETED, FAILED, REFUNDED
- `enums/PaymentMethod` – CASH_ON_DELIVERY, CREDIT_CARD, DEBIT_CARD, BANK_TRANSFER, VNPAY, MOMO, ZALOPAY
- `repository/PaymentRepository` – JPA repository
- `service/PaymentService` (interface), `PaymentServiceImpl` – business logic
- `service/PaymentGatewayService` – mock payment gateway (80% success rate)
- `event/PaymentEvent`, `PaymentEventPublisher`, `PaymentEventListener` – RabbitMQ events
- `dto/*` – `PaymentRequest`, `PaymentResponse`, `PaymentDTO`, `PaymentCallbackDTO`
- `exception/*` – `GlobalExceptionHandler`, `PaymentNotFoundException`, `PaymentFailedException`
- `config/*` – `SecurityConfig`, `RabbitMQConfig`, `OpenApiConfig`
- `resources/db/migration/V1__init_schema.sql` – database schema

### Endpoints chính
Public:
- `POST /payments/process` – Xử lý thanh toán
- `GET /payments/{id}` – Chi tiết payment
- `GET /payments/order/{orderId}` – Payment theo order ID
- `POST /payments/callback` – Callback từ payment gateway

Admin (ROLE_ADMIN, ROLE_SUPER_ADMIN):
- `POST /payments/{id}/refund` – Refund payment
- `GET /payments` – Tất cả payments (phân trang)

### Event Flow (RabbitMQ)
- **OrderReservedEvent** → PaymentService (process payment)
- **PaymentCompletedEvent** → OrderService, WarrantyService, InventoryService
- **PaymentFailedEvent** → OrderService, InventoryService (release reservation)
- **PaymentRefundedEvent** → OrderService

### Payment Gateway (Mock)
- Simulate 80% success rate
- Generate transaction ID
- Simulate processing delay (500ms)
- Throw exception on failure

### Cấu hình
#### `application.yml` (local)
- Port: `8085`
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
cd payment-service
mvn clean package
mvn spring-boot:run
```

Swagger UI:  
`http://localhost:8085/swagger-ui.html`

### Docker
```bash
cd payment-service
mvn clean package -DskipTests
docker build -t payment-service:latest .
docker run -p 8085:8085 payment-service:latest
```

Payment Service được route qua API Gateway với prefix:
- `/api/payments/**`

