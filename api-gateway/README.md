# API Gateway Service

> **Cổng vào duy nhất cho hệ thống Microservices, xử lý routing, authentication, và rate limiting.**

[![Spring Cloud Gateway](https://img.shields.io/badge/Spring%20Cloud%20Gateway-2025.0.0-green.svg)](https://spring.io/projects/spring-cloud-gateway)
[![Spring Security](https://img.shields.io/badge/Spring%20Security-JWT-blue.svg)](https://spring.io/projects/spring-security)
[![Resilience4j](https://img.shields.io/badge/Resilience4j-Circuit%20Breaker-orange.svg)](https://resilience4j.readme.io/)

## 📋 Mục lục

- [Giới thiệu](#-giới-thiệu)
- [Tính năng](#-tính-năng)
- [Routing Rules](#-routing-rules)
- [Security](#-security)
- [Rate Limiting](#-rate-limiting)
- [Cài đặt & Chạy](#-cài-đặt--chạy)
- [Configuration](#%EF%B8%8F-configuration)
- [Monitoring](#-monitoring)

## 🎯 Giới thiệu

API Gateway đóng vai trò là entry point cho tất cả các clients (Web, Mobile). Nó thực hiện các nhiệm vụ cross-cutting concerns như xác thực, định tuyến, giới hạn request, và giám sát.

**Port:** `8181`

## ✨ Tính năng

- ✅ **Routing**: Định tuyến request đến đúng microservice dựa trên đường dẫn.
- ✅ **Authentication**: Validate JWT token cho các secured endpoints.
- ✅ **Header Propagation**: Chuyển thông tin User (ID, Email, Roles) vào header request.
- ✅ **Rate Limiting**: Giới hạn 100 requests/phút mỗi user/IP sử dụng Redis.
- ✅ **Circuit Breaker**: Bảo vệ hệ thống khi downstream service bị lỗi.
- ✅ **CORS**: Cấu hình Cross-Origin cho Frontend development.
- ✅ **Correlation ID**: Gán Request ID và Trace ID để theo dõi request.

## 🛣️ Routing Rules

Tất cả request bắt đầu bằng `/api` sẽ được rewrite và forward:

| Path Prefix | Destination Service | Rewrite Rule | Auth Required |
|-------------|---------------------|--------------|---------------|
| `/api/auth/**` | `user-service` | `/auth/**` | ❌ No |
| `/api/users/**` | `user-service` | `/users/**` | ✅ Yes |
| `/api/products/**` | `product-service` | `/products/**` | ✅ Yes |
| `/api/categories/**`| `product-service` | `/categories/**`| ✅ Yes |
| `/api/carts/**` | `cart-service` | `/carts/**` | ✅ Yes |
| `/api/orders/**` | `order-service` | `/orders/**` | ✅ Yes |
| `/api/inventory/**` | `inventory-service` | `/inventory/**` | ✅ Yes |
| `/api/payments/**` | `payment-service` | `/payments/**` | ✅ Yes |
| `/api/warranties/**`| `warranty-service` | `/warranties/**`| ✅ Yes |
| `/api/recommendations/**` | `recommendation-service` | `/recommendations/**` | ✅ Yes |

## 🔒 Security

### JWT Validation
- Gateway kiểm tra header `Authorization: Bearer <token>`
- Token hợp lệ -> Extract User info -> Set headers:
  - `X-User-Id`
  - `X-User-Email`
  - `X-User-Roles`
- Token không hợp lệ/hết hạn -> Trả về `401 Unauthorized`

### Excluded Paths
Các path không cần token:
- `/api/auth/login`
- `/api/auth/register`
- `/api/auth/refresh`
- `/actuator/**`
- `/swagger-ui/**`
- `/v3/api-docs/**`

## 🚦 Rate Limiting

Sử dụng Redis để đếm request (Sliding Window hoặc Fixed Window đơn giản).

- **Limit**: 100 requests / 1 phút
- **Key**:
  - Authenticated user: `rate_limit:user:{userId}`
  - Anonymous user: `rate_limit:ip:{ipAddress}`
- **Response Header**:
  - `X-RateLimit-Limit`: 100
  - `X-RateLimit-Remaining`: số lượt còn lại
- **Exceeded**: Trả về `429 Too Many Requests`

## 🚀 Cài đặt & Chạy

### Prerequisites
- Redis đang chạy (Port 6379)
- Discovery Service đang chạy (Port 8761)

### Environment Variables
| Variable | Description | Default |
|----------|-------------|---------|
| `JWT_SECRET` | Secret key để verify token | (dev default) |
| `SPRING_REDIS_HOST` | Redis Host | localhost |

### Run Service

```bash
# Local
mvn spring-boot:run

# Docker
docker build -t api-gateway .
docker run -p 8181:8181 --link redis --link discovery-service api-gateway
```

## ⚙️ Configuration

### Circuit Breaker (Resilience4j)
Mỗi route được bảo vệ bởi một Circuit Breaker riêng.
- Failure threshold: 50%
- Wait duration in open state: 5s
- Sliding window size: 10

## 📊 Monitoring

Gateway expose các metrics qua Actuator:
- `http://localhost:8181/actuator/gateway/routes`: Xem các routes hiện tại
- `http://localhost:8181/actuator/metrics`: JVM & Gateway metrics
- `http://localhost:8181/actuator/prometheus`: Prometheus integration

---
**API Gateway Service** - Part of AI Agent E-commerce System
