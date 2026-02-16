## Product Service

### Chức năng

- Quản lý sản phẩm và danh mục:
  - Tạo/sửa/xoá sản phẩm.
  - Quản lý category.
- Tìm kiếm, phân trang, lọc sản phẩm.
- Được dùng bởi giỏ hàng, recommendation, v.v.

### Cổng

- Mặc định: `8082`

### Endpoint chính (tham khảo)

- Sản phẩm:
  - `GET /products` – danh sách (paginated).
  - `GET /products/{id}` – chi tiết sản phẩm.
  - `GET /products/search` – tìm kiếm nâng cao.
  - `GET /products/category/{categoryId}` – sản phẩm theo category.
  - `POST /products` – tạo mới (ADMIN).
  - `PUT /products/{id}` – cập nhật (ADMIN).
  - `DELETE /products/{id}` – xoá (ADMIN).
- Category:
  - `GET /categories`
  - `POST /categories` (ADMIN)
  - `PUT /categories/{id}` (ADMIN)
  - `DELETE /categories/{id}` (ADMIN)

### Bảo mật

- SecurityConfig:
  - Cho phép `/products/**`, `/categories/**`, swagger, actuator.
  - Không decode JWT.
- Controller:
  - Các thao tác ghi (POST/PUT/DELETE) yêu cầu ADMIN/SUPER_ADMIN qua header `X-User-Roles`.

### Persistence

- Entities:
  - `Product`, `Category` (kế thừa `BaseEntity`).
- Flyway migrations:
  - `db/migration/V1__init_schema.sql` định nghĩa bảng, khoá ngoại, index.

### Tích hợp

- Được gọi bởi:
  - `cart-service` qua Feign để lấy `productName`, `price`.
  - `recommendation-service` để build `RecommendationDTO`.

### Chạy service

```bash
mvn spring-boot:run
```

## Product Service (Port 8082)

**Chức năng**: Quản lý **product catalog** cho hệ thống e-commerce:
- Products, Categories
- Product Images, Product Specs
- Tìm kiếm, phân trang, filter theo nhiều tiêu chí

Service này được gọi trực tiếp từ client (qua API Gateway) và gián tiếp từ các service khác (Cart, Recommendation, v.v.).

### Tech
- Spring Boot 3.5.7, Java 21
- Spring Data JPA (PostgreSQL) + Flyway
- Redis Cache (Spring Cache)
- RabbitMQ events
- Eureka Client
- Spring Boot Actuator
- SpringDoc OpenAPI (Swagger UI)

### Cấu trúc chính
- `ProductServiceApplication` – main class `@EnableDiscoveryClient`
- `controller/*`  
  - `ProductController` – CRUD + search products  
  - `CategoryController` – CRUD categories
- `dto/*` – `ProductDTO`, `ProductCreateRequest`, `ProductUpdateRequest`, `ProductSearchRequest`, `CategoryDTO`, `PageResponse`
- `entity/*` – `Product`, `Category`, `ProductImage`, `ProductSpec`, `BaseEntity`
- `repository/*` – `ProductRepository` (có search query), `CategoryRepository`, `ProductImageRepository`, `ProductSpecRepository`
- `service/*` – `ProductServiceImpl`, `CategoryServiceImpl` (có caching)
- `mapper/*` – MapStruct mappers
- `event/*` – `ProductEventPublisher` phát sự kiện tạo/cập nhật/xoá sản phẩm qua RabbitMQ
- `exception/*` – `GlobalExceptionHandler`, `ProductNotFoundException`, `CategoryNotFoundException`
- `config/*` – `SecurityConfig`, `RedisConfig`, `RabbitMQConfig`, `OpenApiConfig`
- `resources/db/migration/V1__init_schema.sql` – schema PostgreSQL cho product catalog

### Endpoints chính
Public:
- `GET /products` – danh sách (phân trang)
- `GET /products/{id}` – chi tiết sản phẩm
- `GET /products/search` – tìm kiếm nâng cao
- `GET /products/category/{categoryId}` – theo category
- `GET /categories` – tất cả category
- `GET /categories/{id}` – chi tiết category

Admin (ROLE_ADMIN, ROLE_SUPER_ADMIN):
- `POST /products`
- `PUT /products/{id}`
- `DELETE /products/{id}`
- `POST /categories`
- `PUT /categories/{id}`
- `DELETE /categories/{id}`

### Cấu hình
#### `application.yml` (local)
- Port: `8082`
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
cd product-service
mvn clean package
mvn spring-boot:run
```

Swagger UI:  
`http://localhost:8082/swagger-ui.html`

### Docker
```bash
cd product-service
mvn clean package -DskipTests
docker build -t product-service:latest .
docker run -p 8082:8082 product-service:latest
```

