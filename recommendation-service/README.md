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
