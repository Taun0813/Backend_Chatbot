## Inventory Service (Port 8083)

**Chức năng**: Quản lý tồn kho và đặt giữ hàng (reservation) cho các đơn hàng.  
Service này được gọi từ `order-service` / `payment-service` để:
- Kiểm tra tồn kho cho từng `productId`
- Đặt giữ lượng tồn khi tạo đơn (reservation)
- Chuẩn bị cho các bước confirm/release sau khi thanh toán

### Tech
- Spring Boot 3.5.7, Java 21
- Spring Data JPA (PostgreSQL)
- Eureka Client (service discovery)
- Spring Boot Actuator (health, metrics)

### Cấu trúc chính
- `InventoryServiceApplication` – main class với `@EnableDiscoveryClient`
- `controller/InventoryController`  
  - `GET /inventories/{productId}` – xem tồn kho hiện tại cho sản phẩm  
  - `POST /inventories/reserve` – đặt giữ tồn kho (simple flow)
- `model/Inventory` – bảng `inventories_chatbot` (availableStock, reservedStock, version)  
- `model/InventoryReservation` – bảng `inventory_reservations` (reservation cho từng order)  
- `service/InventoryService` – nghiệp vụ `reserve()` với pessimistic lock
- `repository/*` – JPA repositories
- `dto/Request`, `dto/Response` – request/response cho API `reserve`

### Cấu hình
#### `application.yml` (local)
- Port: `8083`
- DB: `jdbc:postgresql://localhost:5433/ai_agent`
- JPA: `ddl-auto=update`
- Eureka: `http://localhost:8761/eureka/`
- Actuator:
  - `/actuator/health`
  - `/actuator/metrics`
  - `/actuator/prometheus`

#### `application-docker.yml`
- DB: `postgresql:5432/ai_agent`
- Eureka: `http://ai-agent-discovery:8761/eureka/`

### Chạy local
```bash
cd inventory-service
mvn clean package
mvn spring-boot:run
```

### Docker
Dockerfile sử dụng:
```dockerfile
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY target/inventory-service-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Build & run:
```bash
cd inventory-service
mvn clean package -DskipTests
docker build -t inventory-service:latest .
docker run -p 8083:8083 inventory-service:latest
```

