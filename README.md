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
| **api-gateway** | 8080 | Spring Cloud Gateway | ✅ Ready | Cổng vào duy nhất, Auth filter |
| **user-service** | 8084 | Spring Boot | ✅ Ready | Quản lý User, Auth (JWT) |
| **product-service** | 8082 | Spring Boot | ✅ Ready | Quản lý sản phẩm, Specs |
| **payment-service** | TBD | Spring Boot | 🚧 In Progress | Tích hợp cổng thanh toán |
| **order-service** | 8085 | Spring Boot | ⏳ Pending | Quản lý đơn hàng |
| **warranty-service** | 8086 | Spring Boot | ⏳ Pending | Quản lý bảo hành điện tử |
| **ai-agent-integration**| 8083 | Spring Boot | ⏳ Pending | Cầu nối Java <-> Python |
| **python-ai-agent** | 8000 | FastAPI/LangChain | ⏳ Pending | Xử lý LLM, RAG Logic |

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

## 📚 Database Schema (Tóm tắt)

Hệ thống sử dụng **Database per Service** pattern.

1.  **User DB**: `users`, `addresses`
2.  **Product DB**: `products`, `product_specs`, `categories`
3.  **Order DB**: `orders`, `order_items`
4.  **Warranty DB**: `warranties`, `warranty_claims`
5.  **Payment DB**: `payments`

*(Chi tiết xem file `structure.txt`)*

---

## 🚀 Roadmap Phát triển

### Phase 1: Foundation (Hiện tại)
- [x] Thiết lập Project Structure (Parent POM).
- [x] Cấu hình Service Discovery (Eureka).
- [x] Cấu hình API Gateway.
- [x] Implement User Service & Product Service cơ bản.

### Phase 2: Core Business Logic (Tiếp theo)
- [ ] **Order Service**: Tạo đơn, quản lý trạng thái đơn hàng.
- [ ] **Payment Service**: Xử lý thanh toán giả lập/VNPAY.
- [ ] **Inter-service Communication**: Dùng Feign Client để Order gọi sang Product (check kho) và User.

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
