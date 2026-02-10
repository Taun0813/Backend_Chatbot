## Order Service (Port 8084)

**Chức năng**: Quản lý đơn hàng và orchestrate order saga:
- Tạo đơn hàng từ giỏ hàng
- Quản lý trạng thái đơn hàng (PENDING → RESERVED → PAID → CONFIRMED → SHIPPED → DELIVERED)
- Saga orchestration với Inventory và Payment services qua RabbitMQ events
- Lịch sử thay đổi trạng thái đơn hàng

### Tech
- Spring Boot 3.5.7, Java 21
- Spring Data JPA (PostgreSQL) + Flyway
- RabbitMQ (events)
- Redis (caching)
- Eureka Client
- Spring Boot Actuator
- SpringDoc OpenAPI (Swagger UI)
- Feign Client (gọi Inventory Service)

### Cấu trúc chính
- `OrderServiceApplication` – main class với `@EnableDiscoveryClient`, `@EnableFeignClients`
- `controller/OrderController` – REST endpoints với Swagger annotations
- `model/Order`, `OrderItem`, `OrderStatusHistory`
- `enums/OrderStatus` – enum với đầy đủ trạng thái
- `repository/*` – `OrderRepository`, `OrderItemRepository`, `OrderStatusHistoryRepository`
- `service/OrderService` (interface), `OrderServiceImpl` – business logic
- `service/OrderSagaOrchestrator` – xử lý saga events
- `event/OrderEvent`, `OrderEventPublisher`, `OrderEventListener` – RabbitMQ events
- `client/InventoryClient` – Feign client gọi Inventory Service
- `dto/*` – `OrderDTO`, `OrderItemDTO`, `CreateOrderRequest`, `OrderStatusUpdateRequest`
- `exception/*` – `GlobalExceptionHandler`, `OrderNotFoundException`, `InvalidOrderStatusException`
- `config/*` – `SecurityConfig`, `RabbitMQConfig`, `OpenApiConfig`
- `resources/db/migration/V1__init_schema.sql` – database schema

### Order Status Flow
```
PENDING → RESERVED → PAYMENT_PENDING → PAYMENT_PROCESSING → PAID → CONFIRMED → PROCESSING → SHIPPED → DELIVERED
   ↓                                                                      ↓
CANCELLED                                                              FAILED
```

### Endpoints chính
Public:
- `POST /orders` – Tạo đơn hàng mới
- `GET /orders/{id}` – Chi tiết đơn hàng
- `GET /orders/user/{userId}` – Danh sách đơn của user
- `GET /orders/user/me` – Đơn hàng của user hiện tại
- `PUT /orders/{id}/cancel` – Hủy đơn hàng
- `GET /orders/{id}/status-history` – Lịch sử trạng thái

Admin (ROLE_ADMIN, ROLE_SUPER_ADMIN):
- `PUT /orders/{id}/status` – Cập nhật trạng thái đơn hàng
- `GET /orders` – Tất cả đơn hàng (phân trang)

### Event Flow (RabbitMQ)
- **OrderCreatedEvent** → InventoryService (reserve stock)
- **InventoryReservedEvent** → OrderService (update status to RESERVED) → PaymentService
- **PaymentCompletedEvent** → OrderService (update status to PAID/CONFIRMED) → WarrantyService
- **PaymentFailedEvent** → OrderService (update status to FAILED) → InventoryService (release stock)

### Cấu hình
#### `application.yml` (local)
- Port: `8084`
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
cd order-service
mvn clean package
mvn spring-boot:run
```

Swagger UI:  
`http://localhost:8084/swagger-ui.html`

### Docker
```bash
cd order-service
mvn clean package -DskipTests
docker build -t order-service:latest .
docker run -p 8084:8084 order-service:latest
```

Order Service được route qua API Gateway với prefix:
- `/api/orders/**`

