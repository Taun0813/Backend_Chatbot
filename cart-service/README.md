## Cart Service (Port 8086)

**Chức năng**: Quản lý giỏ hàng của người dùng:
- Lưu trữ các sản phẩm trong giỏ của từng user
- Tự động fetch product details từ Product Service
- Tính toán tổng tiền và số lượng tự động
- Ngăn chặn duplicate items (update quantity thay vì tạo mới)
- Xóa giỏ hàng sau khi tạo đơn hàng thành công

### Tech
- Spring Boot 3.5.7, Java 21
- Spring Data JPA (PostgreSQL) + Flyway
- Feign Client (gọi Product Service)
- RabbitMQ (events)
- Redis (caching)
- Eureka Client
- Spring Boot Actuator
- SpringDoc OpenAPI (Swagger UI)

### Cấu trúc chính
- `CartServiceApplication` – main class với `@EnableDiscoveryClient`, `@EnableFeignClients`
- `controller/CartController` – REST endpoints với Swagger annotations
- `model/Cart`, `CartItem` – entities với BigDecimal cho prices, tự động tính totals
- `dto/*` – `CartDTO`, `CartItemDTO`, `AddToCartRequest`, `UpdateCartItemRequest`
- `repository/*` – `CartRepository`, `CartItemRepository`
- `service/CartService` (interface), `CartServiceImpl` – business logic với Feign client
- `client/ProductServiceClient` – Feign client gọi Product Service
- `event/CartEventListener`, `CartEventPublisher` – RabbitMQ events
- `exception/*` – `GlobalExceptionHandler`, `CartNotFoundException`, `CartItemNotFoundException`
- `config/*` – `SecurityConfig`, `RabbitMQConfig`, `OpenApiConfig`
- `resources/db/migration/V1__init_schema.sql` – database schema

### Endpoints chính
Public:
- `GET /carts/me` – Lấy giỏ hàng của user hiện tại
- `POST /carts/items` – Thêm item vào giỏ
- `PUT /carts/items/{itemId}` – Cập nhật số lượng item
- `DELETE /carts/items/{itemId}` – Xóa item khỏi giỏ
- `DELETE /carts/clear` – Xóa toàn bộ giỏ hàng

Admin (ROLE_ADMIN, ROLE_SUPER_ADMIN):
- `GET /carts/{userId}` – Lấy giỏ hàng của user bất kỳ

### Business Logic
- **Auto-create cart**: Tự động tạo giỏ hàng khi user thêm item đầu tiên
- **Fetch product details**: Gọi Product Service qua Feign để lấy tên và giá sản phẩm
- **Calculate totals**: Tự động tính `totalAmount` và `totalItems` khi có thay đổi
- **Prevent duplicates**: Nếu sản phẩm đã có trong giỏ, tăng quantity thay vì tạo mới
- **Clear cart**: Xóa giỏ hàng sau khi order được tạo thành công

### Event Flow (RabbitMQ)
- **OrderCreatedEvent** → CartService (clearCart)
- **ProductDeletedEvent** → CartService (removeProductFromCarts)

### Cấu hình
#### `application.yml` (local)
- Port: `8086`
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
cd cart-service
mvn clean package
mvn spring-boot:run
```

Swagger UI:  
`http://localhost:8086/swagger-ui.html`

### Docker
```bash
cd cart-service
mvn clean package -DskipTests
docker build -t cart-service:latest .
docker run -p 8086:8086 cart-service:latest
```

Cart Service được route qua API Gateway với prefix:
- `/api/carts/**`
