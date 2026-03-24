## E-Commerce Backend Microservices

### Tổng quan

Hệ thống backend microservices cho nền tảng thương mại điện tử, xây dựng với **Spring Boot 3**, **Java 21**, **PostgreSQL**, **Redis**, **RabbitMQ**, **Eureka** và **Spring Cloud Gateway**.

Mục tiêu:

- Kiến trúc microservices rõ ràng, mỗi domain một service.
- API Gateway là entrypoint duy nhất.
- Chỉ Gateway validate JWT; các service bên dưới tin tưởng header `X-User-Id`, `X-User-Roles`.

### Kiến trúc tổng quan

- **Gateway**: `gateway-service` (port 8181)
- **Service discovery**: `discovery-service` (Eureka, port 8761)
- **Domain services:**
  - `user-service` (8081) – user, auth, phân quyền.
  - `product-service` (8082) – sản phẩm, category.
  - `inventory-service` (8083) – tồn kho, reservation.
  - `order-service` (8084) – order, saga orchestration.
  - `payment-service` (8085) – thanh toán (mock gateway).
  - `cart-service` (8086) – giỏ hàng.
  - `recommendation-service` (8087) – gợi ý sản phẩm.
  - `warranty-service` (8088) – bảo hành & claim.

### Công nghệ chính

- **Ngôn ngữ**: Java 21
- **Framework**: Spring Boot 3.x, Spring Cloud
- **Persistence**: Spring Data JPA, Hibernate, Flyway
- **Message broker**: RabbitMQ
- **Cache**: Redis
- **Service discovery**: Eureka Server
- **API docs**: springdoc OpenAPI / Swagger UI
- **Build**: Maven
- **Container**: Docker

### Cấu trúc thư mục

- `Backend_Chatbot/`
  - `discovery-service/`
  - `gateway-service/`
  - `user-service/`
  - `product-service/`
  - `inventory-service/`
  - `order-service/`
  - `payment-service/`
  - `cart-service/`
  - `warranty-service/`
  - `recommendation-service/`

Mỗi service có:

- `pom.xml`
- `Dockerfile`
- `src/main/java/...`
- `src/main/resources/application.yml`
- `src/main/resources/db/migration/V1__init_schema.sql`

### Authentication & Security

- Client gửi JWT trong header `Authorization: Bearer <token>` tới **gateway-service**.
- Gateway validate JWT, trích:
  - `userId` → header `X-User-Id`
  - `roles` → header `X-User-Roles` (ví dụ: `ROLE_USER,ROLE_ADMIN`)
- Các service phía sau:
  - Không decode JWT.
  - Dùng `X-User-Id` cho các endpoint `/me`.
  - Dùng `X-User-Roles` trong controller để kiểm tra quyền ADMIN/SUPER_ADMIN.

### Chạy hệ thống

#### Yêu cầu

- Java 21
- Maven 3.8+
- Docker (PostgreSQL, Redis, RabbitMQ)

#### 1. Khởi chạy hạ tầng

Ví dụ với Docker:

```bash
docker run --name postgres -e POSTGRES_PASSWORD=postgres -p 5432:5432 -d postgres:16
docker run --name redis -p 6379:6379 -d redis:7
docker run --name rabbitmq -p 5672:5672 -p 15672:15672 -d rabbitmq:3-management
```

#### 2. Chạy Eureka Server

```bash
cd Backend_Chatbot/discovery-service
mvn spring-boot:run
```

Eureka UI: `http://localhost:8761`.

#### 3. Chạy các microservice domain

Ví dụ:

```bash
cd Backend_Chatbot/user-service
mvn spring-boot:run

cd Backend_Chatbot/product-service
mvn spring-boot:run

# ... tương tự cho inventory, order, payment, cart, warranty, recommendation
```

Hoặc build jar:

```bash
mvn clean package -DskipTests
java -jar target/<service>-0.0.1-SNAPSHOT.jar
```

#### 4. Chạy API Gateway

```bash
cd Backend_Chatbot/gateway-service
mvn spring-boot:run
```

Gateway: `http://localhost:8181`.

### Chạy bằng Docker

Mỗi service có `Dockerfile`, ví dụ với user-service:

```bash
cd Backend_Chatbot/user-service
docker build -t user-service:latest .
docker run --rm -p 8081:8081 --env-file .env user-service:latest
```

Có thể bổ sung `docker-compose.yml` gom tất cả service + database + redis + rabbitmq.

### API Documentation

Swagger UI của từng service (ví dụ cổng):

- User Service: `http://localhost:8081/swagger-ui/index.html`
- Product Service: `http://localhost:8082/swagger-ui/index.html`
- Inventory Service: `http://localhost:8083/swagger-ui/index.html`
- Order Service: `http://localhost:8084/swagger-ui/index.html`
- Payment Service: `http://localhost:8085/swagger-ui/index.html`
- Cart Service: `http://localhost:8086/swagger-ui/index.html`
- Recommendation Service: `http://localhost:8087/swagger-ui/index.html`
- Warranty Service: `http://localhost:8088/swagger-ui/index.html`

### Saga & Event chính

- Order Saga:
  - Order PENDING → `OrderCreatedEvent` → Inventory reserve.
  - Inventory reserved/fail → `InventoryReservedEvent` / `InventoryReservationFailedEvent`.
  - Payment completed/failed → `PaymentCompletedEvent` / `PaymentFailedEvent`.
  - Khi paid → `OrderPaidEvent` → Warranty auto-create.
- Recommendation:
  - `ProductViewedEvent`, `OrderCompletedEvent` → ghi nhận tương tác user.
- Cart:
  - `OrderCreatedEvent` → clear cart.
  - `ProductDeletedEvent` → remove product khỏi mọi cart.

Chi tiết luồng và endpoint xem thêm trong `README.md` của từng service.
