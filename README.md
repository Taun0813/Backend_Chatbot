# 🤖 AI Agent E-commerce Backend System (Ver 2)

> **Hệ thống Microservices E-commerce tích hợp AI Agent thế hệ mới**
>
> *Status: Active Development*

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2+-green.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-blue.svg)](https://openjdk.java.net/)
[![Python](https://img.shields.io/badge/Python-3.10+-yellow.svg)](https://www.python.org/)
[![Docker](https://img.shields.io/badge/Docker-Compose-blue.svg)](https://www.docker.com/)

## 📖 Tổng quan

Dự án xây dựng nền tảng thương mại điện tử (bán điện thoại/đồ công nghệ) với kiến trúc **Microservices**. Điểm nhấn là **AI Agent** đóng vai trò trợ lý ảo thông minh, có khả năng tư vấn sản phẩm, tra cứu bảo hành và hỗ trợ đặt hàng thông qua hội thoại tự nhiên (RAG - Retrieval Augmented Generation).

---

## 🏗 Kiến trúc Hệ thống

### 1. High-Level Architecture

```mermaid
graph TB
    subgraph "Client Layer"
        WEB[Web/Mobile App]
    end

    subgraph "Gateway Layer"
        LB[Nginx Load Balancer]
        GW[Spring Cloud Gateway]
    end

    subgraph "Service Discovery"
        EUREKA[Eureka Server]
    end

    subgraph "Core Business Services (Java)"
        AUTH[User Service]
        PROD[Product Service]
        ORDER[Order Service]
        PAY[Payment Service]
        WARRANTY[Warranty Service]
    end

    subgraph "AI & Search Layer"
        SEARCH[Search Service (Elastic)]
        AI_INT[AI Integration Service (Java)]
        PY_AGENT[Python AI Agent (LLM + RAG)]
        VEC_DB[(Pinecone/Vector DB)]
    end

    subgraph "Infrastructure"
        DB[(PostgreSQL per Service)]
        CACHE[(Redis)]
        MQ[RabbitMQ]
    end

    WEB --> LB --> GW
    GW --> AUTH
    GW --> PROD
    GW --> ORDER
    GW --> PAY
    GW --> WARRANTY
    GW --> SEARCH
    GW --> AI_INT

    AI_INT <-->|gRPC/REST| PY_AGENT
    PY_AGENT --> VEC_DB
    
    AUTH & PROD & ORDER & PAY & WARRANTY --> DB
    AUTH & PROD --> CACHE
    ORDER --> MQ --> PAY
```

### 2. Trạng thái Modules (Project Status)

| Module Name | Port | Tech Stack | Trạng thái | Mô tả |
|-------------|------|------------|------------|-------|
| **discovery-service** | 8761 | Spring Eureka | ✅ Ready | Service Registry |
| **api-gateway** | 8080 | Spring Cloud Gateway | ✅ Ready | Cổng vào duy nhất, Auth filter, JWT Validation |
| **user-service** | 8084 | Spring Boot | ✅ Ready | Quản lý User, Auth (JWT), Refresh Token |
| **product-service** | 8082 | Spring Boot | ✅ Ready | Quản lý sản phẩm, Specs |
| **payment-service** | TBD | Spring Boot | 🚧 In Progress | Tích hợp cổng thanh toán |
| **order-service** | 8085 | Spring Boot | ⏳ Pending | Quản lý đơn hàng |
| **warranty-service** | 8086 | Spring Boot | ⏳ Pending | Quản lý bảo hành điện tử |
| **ai-agent-integration**| 8083 | Spring Boot | ⏳ Pending | Cầu nối Java <-> Python |
| **python-ai-agent** | 8000 | FastAPI/LangChain | ⏳ Pending | Xử lý LLM, RAG Logic |

---

## 🔐 Authentication & Security Flow

Hệ thống sử dụng cơ chế **Stateless Authentication** với JWT.

1.  **Login Flow:**
    *   Client gửi credentials -> `api-gateway` -> `user-service`.
    *   `user-service` xác thực và trả về cặp `accessToken` (ngắn hạn) và `refreshToken` (dài hạn).
    *   `refreshToken` được lưu trong Database của `user-service` để quản lý phiên đăng nhập.

2.  **Request Flow:**
    *   Client gửi request kèm Header `Authorization: Bearer <token>`.
    *   `api-gateway` chặn request tại `JwtAuthenticationFilter`.
    *   Gateway validate token (signature, expiration).
    *   Nếu hợp lệ, Gateway trích xuất `userId`, `roles` và gắn vào Header (`X-User-Id`, `X-User-Roles`) trước khi forward xuống service đích.

3.  **Refresh Token Flow:**
    *   Khi `accessToken` hết hạn, Client gọi API `/users/refresh-token` tại `user-service`.
    *   `user-service` kiểm tra `refreshToken` trong DB. Nếu còn hạn -> cấp `accessToken` mới.

---

## 🛠 Cài đặt & Triển khai

### Yêu cầu
- Java 21 (JDK)
- Docker & Docker Compose
- Maven 3.8+

### Bước 1: Khởi chạy Infrastructure
Trước khi chạy ứng dụng, cần khởi tạo Database, Cache và Message Queue.

```bash
# Tại thư mục gốc
docker-compose up -d postgresql redis rabbitmq
```

### Bước 2: Build & Run Services
*(Lưu ý: Cần đảm bảo tất cả các module con đã được cấu hình trong pom.xml)*

```bash
# Build toàn bộ project
mvn clean install -DskipTests

# Chạy Docker Compose cho toàn bộ hệ thống
docker-compose up -d
```

---

## 📚 API Documentation (Swagger)

Hệ thống tích hợp Swagger UI để document API.

*   **User Service:** `http://localhost:8084/swagger-ui/index.html`
*   **Product Service:** `http://localhost:8082/swagger-ui/index.html`
*   *(Các service khác sẽ được cập nhật sau)*

---

## 🚀 Roadmap Phát triển & Next Steps

### Phase 1: Foundation (Đã hoàn thành)
- [x] Thiết lập Project Structure (Parent POM).
- [x] Cấu hình Service Discovery (Eureka).
- [x] **API Gateway**: Routing, JWT Filter, Global Exception Handling.
- [x] **User Service**: Login, Register, Refresh Token, Swagger Integration.

### Phase 2: Core Business Logic (Cần làm ngay)
- [ ] **User Service**:
    - [ ] Implement API `POST /users/refresh-token`.
    - [ ] Implement API `POST /users/logout` (xóa refresh token).
    - [ ] Implement API `GET /users/me` (lấy thông tin user từ token).
- [ ] **Product Service**:
    - [ ] Hoàn thiện CRUD sản phẩm.
    - [ ] Tích hợp Swagger.
- [ ] **Order Service**:
    - [ ] Thiết kế DB Schema cho Order.
    - [ ] Implement luồng tạo đơn hàng (gọi sang Product Service để check tồn kho).

### Phase 3: AI Integration (Quan trọng)
- [ ] Xây dựng **Python AI Agent** (FastAPI).
- [ ] Xây dựng **AI Integration Service** (Java) để nhận request từ Gateway và đẩy sang Python.
- [ ] Implement RAG: Sync dữ liệu Product sang Vector DB để AI tra cứu.

### Phase 4: Monitoring & Polish
- [ ] Cấu hình Prometheus & Grafana dashboard.
- [ ] Centralized Logging (ELK Stack).
- [ ] UI Integration.

---

## 🤝 Contribution
Dự án được phát triển bởi team AI Agent. Vui lòng tuân thủ coding convention đã thống nhất.
