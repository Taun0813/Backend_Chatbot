## Recommendation Service

### Chức năng

- Gợi ý sản phẩm cho người dùng:
  - Gợi ý cá nhân hóa (personalized).
  - Sản phẩm phổ biến (popular).
  - Sản phẩm trending.
  - Sản phẩm tương tự (similar/related).
- Ghi nhận tương tác người dùng với sản phẩm.

### Cổng

- Mặc định: `8087`

### Schema

- `user_preferences`:
  - `user_id`, `product_id`, `category_id`
  - `view_count`, `purchase_count`
  - `last_viewed_at`, `last_purchased_at`
  - `preference_score` (DECIMAL 5,2)
  - timestamps
- `product_recommendations`:
  - `user_id` (có thể null nếu global)
  - `product_id`
  - `recommendation_type` (POPULAR, PERSONALIZED, RELATED, TRENDING)
  - `score`
  - `rank_position`
  - timestamps

### Enum InteractionType

```java
VIEW(1.0),           // xem sản phẩm
ADD_TO_CART(3.0),    // thêm vào giỏ
PURCHASE(5.0),       // mua hàng
REMOVE_FROM_CART(-1.0); // bỏ khỏi giỏ
```

### Endpoint chính (theo spec)

- `GET /recommendations/me`
  - Trả về gợi ý cá nhân hóa (PERSONALIZED) cho user hiện tại.
- `POST /recommendations/track`
  - Body `InteractionDTO { productId, categoryId, interactionType }`.
  - Dùng `X-User-Id` làm nguồn userId.
- `GET /recommendations/popular`
  - Trả về danh sách sản phẩm phổ biến (POPULAR).
- `GET /recommendations/similar/{productId}`
  - Trả về danh sách sản phẩm tương tự/related.

Thêm:

- `GET /recommendations/user/preferences`
  - Trả về danh sách `UserPreferenceDTO` cho user hiện tại.
- `POST /recommendations/refresh/{type}` (ADMIN)
  - Refresh dữ liệu trong bảng `product_recommendations` cho từng `RecommendationType`.

### Business logic (collaborative filtering đơn giản)

1. Lưu tương tác người dùng vào `user_preferences`:
   - Tăng `view_count` khi VIEW/ADD_TO_CART.
   - Tăng `purchase_count` khi PURCHASE.
   - Cập nhật `preference_score` (views * 0.1 + purchases * 1.0).
2. Khi refresh:
   - `POPULAR`:
     - Gom purchaseCount theo product, sort giảm dần, lưu top vào `product_recommendations` với type POPULAR.
   - `PERSONALIZED`:
     - Với mỗi user, lấy top sản phẩm theo `preference_score`, lưu vào `product_recommendations` type PERSONALIZED.
   - `TRENDING`:
     - Chọn sản phẩm có hoạt động trong 7 ngày gần nhất, sort theo score, lưu type TRENDING.
   - `RELATED`:
     - Sinh on-demand từ các sản phẩm mà user có hành vi tương tự (không cần lưu sẵn).

### Sự kiện

- `RecommendationEventListener`:
  - `OrderCompletedEvent` (từ `order.exchange`, routing key `order.completed`):
    - Gọi `recordPurchase(userId, productId, categoryId)` cho từng item trong order.
  - `ProductViewedEvent` (từ `recommendation.exchange`, routing key `product.viewed`):
    - Gọi `recordView(userId, productId, categoryId)`.

### Bảo mật

- SecurityConfig:
  - Cho phép `/recommendations/**`, swagger, actuator.
- Controller:
  - `refreshRecommendations` yêu cầu ADMIN/SUPER_ADMIN, kiểm tra qua `X-User-Roles`.
  - Các endpoint còn lại:
    - Dùng `X-User-Id` cho những nơi cần user cụ thể (`/me`, `/user/preferences`).

### Tích hợp với Product Service

- Feign `ProductServiceClient`:
  - Dùng để lấy thông tin chi tiết sản phẩm khi build `RecommendationDTO`:
    - `productId`, `productName`, `description`, `price`, `categoryId`, `categoryName`.

### Chạy service

```bash
mvn spring-boot:run
```

## Recommendation Service (Port 8089)

**Chức năng**: Gợi ý sản phẩm cho người dùng dựa trên hành vi:
- Theo dõi lịch sử xem sản phẩm (view)
- Theo dõi lịch sử mua hàng (purchase)
- Tính toán preference score dựa trên views và purchases
- Tạo recommendations theo nhiều loại: POPULAR, PERSONALIZED, RELATED, TRENDING

### Tech
- Spring Boot 3.5.7, Java 21
- Spring Data JPA (PostgreSQL) + Flyway
- RabbitMQ (events)
- Redis (caching)
- Eureka Client
- Spring Boot Actuator
- SpringDoc OpenAPI (Swagger UI)
- Feign Client (gọi Product Service)

### Cấu trúc chính
- `RecommendationServiceApplication` – main class với `@EnableDiscoveryClient`, `@EnableFeignClients`, `@EnableCaching`
- `controller/RecommendationController` – REST endpoints với Swagger annotations
- `entity/UserPreference` – lưu trữ user behavior (view count, purchase count, preference score)
- `entity/ProductRecommendation` – lưu trữ recommendations đã tính toán
- `enums/RecommendationType` – POPULAR, PERSONALIZED, RELATED, TRENDING
- `repository/*` – `UserPreferenceRepository`, `ProductRecommendationRepository`
- `service/RecommendationService` (interface), `RecommendationServiceImpl` – business logic với caching
- `event/RecommendationEventListener` – lắng nghe OrderCompletedEvent và ProductViewedEvent
- `client/ProductServiceClient` – Feign client để lấy product details
- `dto/*` – `RecommendationDTO`, `UserPreferenceDTO`
- `exception/*` – `GlobalExceptionHandler`
- `config/*` – `SecurityConfig`, `RabbitMQConfig`, `OpenApiConfig`, `CacheConfig`
- `resources/db/migration/V1__init_schema.sql` – database schema

### Business Logic
- **Preference Score**: Tính toán dựa trên `viewCount * 0.1 + purchaseCount * 1.0`
- **POPULAR**: Sản phẩm có tổng purchase count cao nhất
- **PERSONALIZED**: Recommendations dựa trên user preferences (top 20 products theo preference score)
- **TRENDING**: Sản phẩm được xem/mua nhiều trong 7 ngày gần đây
- **RELATED**: Sản phẩm thường được xem/mua cùng với sản phẩm hiện tại (collaborative filtering)
- **Caching**: Recommendations được cache để tăng performance

### Endpoints chính
Public:
- `GET /recommendations/{type}` – Lấy recommendations theo type (POPULAR, PERSONALIZED, TRENDING)
  - Query params: `limit` (default: 10)
  - Nếu có `X-User-Id` header, trả về personalized recommendations
- `GET /recommendations/product/{productId}/related` – Lấy related products
- `GET /recommendations/user/preferences` – Lấy user preferences (cần `X-User-Id` header)

Admin (ROLE_ADMIN, ROLE_SUPER_ADMIN):
- `POST /recommendations/refresh/{type}` – Refresh recommendations (tính toán lại)

### Event Flow (RabbitMQ)
- **OrderCompletedEvent** → Record purchase, refresh personalized recommendations
- **ProductViewedEvent** → Record view, update preference score

### Cấu hình
#### `application.yml` (local)
- Port: `8089`
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
cd recommendation-service
mvn clean package
mvn spring-boot:run
```

Swagger UI:  
`http://localhost:8089/swagger-ui.html`

### Docker
```bash
cd recommendation-service
mvn clean package -DskipTests
docker build -t recommendation-service:latest .
docker run -p 8089:8089 recommendation-service:latest
```

Recommendation Service được route qua API Gateway với prefix:
- `/api/recommendations/**`

### Algorithm Notes
- **Simple Collaborative Filtering**: Tìm users có cùng preferences, sau đó recommend products họ đã xem/mua
- **Scoring**: Views có weight 0.1, Purchases có weight 1.0
- **Refresh Strategy**: 
  - POPULAR và TRENDING: Refresh định kỳ (có thể schedule)
  - PERSONALIZED: Refresh khi user purchase
  - RELATED: Tính toán on-demand
