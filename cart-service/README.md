## Cart Service

### Chức năng

- Quản lý giỏ hàng của người dùng:
  - Auto-create cart khi user thêm sản phẩm lần đầu.
  - Thêm/sửa/xoá sản phẩm trong giỏ.
  - Tự tính subtotal, tổng tiền, tổng số lượng.
  - Clear cart khi order được tạo thành công.
- Tích hợp `product-service` để lấy thông tin sản phẩm.

### Cổng

- Mặc định: `8086`

### Bảng chính

- `carts`:
  - `user_id` (unique), `total_amount`, `total_items`, timestamps.
- `cart_items`:
  - `cart_id`, `product_id`, `product_name`, `product_price`, `quantity`, `subtotal`, timestamps.

### Endpoint chính

- `GET /carts/me` – lấy giỏ hàng của user hiện tại.
- `POST /carts/items` – thêm 1 sản phẩm vào giỏ (body `AddToCartRequest`).
- `PUT /carts/items/{itemId}` – cập nhật quantity (body `UpdateCartItemRequest`).
- `DELETE /carts/items/{itemId}` – xoá 1 item theo id.
- `DELETE /carts/clear` – clear toàn bộ giỏ.
- `GET /carts/{userId}` – xem giỏ hàng của user bất kỳ (ADMIN).

### Business logic

1. Auto-create cart khi chưa tồn tại và user thêm item.
2. Fetch product từ `product-service`:
   - Qua Feign `ProductServiceClient.getProductById(productId)`.
   - Dùng `name`, `price` từ ProductDTO.
3. Nếu product đã có trong giỏ:
   - Chỉ tăng quantity, cập nhật subtotal.
4. `updateCartTotals`:
   - `total_items` = tổng quantity của mọi item.
   - `total_amount` = tổng subtotal.
5. Sau khi `OrderCreatedEvent`:
   - `CartEventListener` gọi `clearCart(userId)`.
6. Khi `ProductDeletedEvent`:
   - Xoá sản phẩm đó khỏi mọi giỏ (`removeProductFromCarts`), cập nhật lại totals.

### Sự kiện

- Từ RabbitMQ:
  - `order.created` (từ `order.exchange`) → clear cart user tương ứng.
  - `product.deleted` (từ `domain-events-exchange`) → xoá sản phẩm khỏi tất cả cart.

### Bảo mật

- SecurityConfig:
  - Cho phép `/carts/**`.
- Controller:
  - `GET /carts/{userId}` yêu cầu ADMIN/SUPER_ADMIN, check `X-User-Roles`.

### Chạy service

```bash
mvn spring-boot:run
```

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
