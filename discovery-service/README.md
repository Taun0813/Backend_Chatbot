## Discovery Service (Eureka Server)

### Chức năng

- Đóng vai trò service registry cho toàn bộ hệ thống.
- Các microservice đăng ký/renew với Eureka.
- Gateway và các service khác dùng service-id để gọi qua load balancing.

### Công nghệ

- Spring Boot 3
- Spring Cloud Netflix Eureka Server

### Cổng

- Mặc định: `8761`

### Endpoint

- Eureka Dashboard: `http://localhost:8761`

### Chạy service

```bash
mvn spring-boot:run
```

Các service khác cấu hình:

```yaml
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

# Discovery Service (Eureka Server)

> **Service Registry và Discovery Server cho hệ thống microservices**

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-green.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-blue.svg)](https://openjdk.java.net/)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2025.0.0-blue.svg)](https://spring.io/projects/spring-cloud)

## 📋 Mục lục

- [Giới thiệu](#-giới-thiệu)
- [Tính năng](#-tính-năng)
- [Tech Stack](#-tech-stack)
- [Cấu trúc Project](#-cấu-trúc-project)
- [Cài đặt](#-cài-đặt)
- [Chạy Service](#-chạy-service)
- [Configuration](#%EF%B8%8F-configuration)
- [Endpoints](#-endpoints)
- [Monitoring](#-monitoring)
- [Docker](#-docker)

## 🎯 Giới thiệu

Discovery Service là **Eureka Server** - service registry trung tâm cho toàn bộ hệ thống microservices. Tất cả các services khác sẽ đăng ký với Eureka để có thể tự động discovery và load balancing.

**Vai trò:**
- 🔍 Service Registration: Các services đăng ký khi khởi động
- 🔎 Service Discovery: Cho phép services tìm và gọi nhau
- 💓 Health Check: Theo dõi trạng thái của các services
- ⚖️ Load Balancing: Hỗ trợ phân tải giữa các instances

## ✨ Tính năng

- ✅ **Service Registry**: Đăng ký và quản lý tất cả microservices
- ✅ **Self-preservation Mode**: Tắt để môi trường dev/test
- ✅ **Health Monitoring**: Actuator endpoints với Prometheus metrics
- ✅ **Eviction Timer**: 5s để nhanh chóng phát hiện services down
- ✅ **Docker Support**: Cấu hình riêng cho Docker environment
- ✅ **Production Ready**: JVM optimization, logging

## 🛠️ Tech Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| Spring Boot | 3.5.7 | Main framework |
| Java | 21 | Programming language |
| Spring Cloud Netflix Eureka | 2025.0.0 | Service Discovery |
| Micrometer | Latest | Metrics collection |
| Prometheus | - | Metrics export |

## 📁 Cấu trúc Project

```
discovery-service/
├── src/
│   ├── main/
│   │   ├── java/vn/tt/practice/discoveryservice/
│   │   │   └── DiscoveryServiceApplication.java    # Main application
│   │   └── resources/
│   │       ├── application.yml                      # Local config
│   │       └── application-docker.yml               # Docker config
│   └── test/
│       └── java/vn/tt/practice/discoveryservice/
│           └── DiscoveryServiceApplicationTests.java
├── Dockerfile                                       # Docker image
├── pom.xml                                          # Maven dependencies
└── README.md                                        # This file
```

## 🚀 Cài đặt

### Prerequisites

- Java 21+
- Maven 3.8+
- (Optional) Docker

### Build

```bash
# Build project
mvn clean package

# Skip tests
mvn clean package -DskipTests
```

## ▶️ Chạy Service

### Cách 1: Run với Maven

```bash
mvn spring-boot:run
```

### Cách 2: Run JAR file

```bash
java -jar target/discovery-service-0.0.1-SNAPSHOT.jar
```

### Cách 3: Run với Docker

```bash
# Build Docker image
docker build -t discovery-service:latest .

# Run container
docker run -p 8761:8761 discovery-service:latest
```

### Cách 4: Run với Docker Compose

```bash
# Từ root directory
docker-compose up -d discovery-service
```

## ⚙️ Configuration

### Local Environment (`application.yml`)

```yaml
server:
  port: 8761

eureka:
  client:
    register-with-eureka: false    # Không đăng ký bản thân
    fetch-registry: false         # Không fetch registry
  server:
    enable-self-preservation: false  # Tắt self-preservation
```

### Docker Environment (`application-docker.yml`)

```yaml
eureka:
  instance:
    hostname: discovery-service
  client:
    service-url:
      defaultZone: http://discovery-service:8761/eureka/
  server:
    eviction-interval-timer-in-ms: 5000  # 5 giây
```

### JVM Settings

**Memory optimization trong Dockerfile:**
```
-Xmx512m    # Max heap size
-Xms256m    # Initial heap size
```

## 🌐 Endpoints

### Eureka Dashboard

```
http://localhost:8761
```

**Dashboard hiển thị:**
- Registered instances
- Application health status
- Instance metadata
- Lease information

### Actuator Endpoints

| Endpoint | URL | Description |
|----------|-----|-------------|
| Health | `http://localhost:8761/actuator/health` | Health check |
| Info | `http://localhost:8761/actuator/info` | Service info |
| Metrics | `http://localhost:8761/actuator/metrics` | JVM metrics |
| Prometheus | `http://localhost:8761/actuator/prometheus` | Prometheus metrics |

## 📊 Monitoring

### Prometheus Metrics

Discovery service expose metrics cho Prometheus:

```bash
curl http://localhost:8761/actuator/prometheus
```

**Metrics quan trọng:**
- `eureka_server_registry_size`: Số lượng services đăng ký
- `eureka_server_renewal_threshold`: Renewal threshold
- `jvm_memory_used_bytes`: JVM memory usage
- `system_cpu_usage`: CPU usage

### Health Check

```bash
curl http://localhost:8761/actuator/health
```

**Response:**
```json
{
  "status": "UP",
  "components": {
    "diskSpace": {"status": "UP"},
    "ping": {"status": "UP"}
  }
}
```

## 🐳 Docker

### Build Image

```bash
docker build -t discovery-service:latest .
```

### Run Container

```bash
docker run -d \
  --name discovery-service \
  -p 8761:8761 \
  discovery-service:latest
```

### Docker Compose

```yaml
discovery-service:
  build: ./discovery-service
  container_name: discovery-service
  ports:
    - "8761:8761"
  environment:
    - SPRING_PROFILES_ACTIVE=docker
  networks:
    - microservices-network
```

## 🔍 Troubleshooting

### Port đã được sử dụng

```bash
# Windows
netstat -ano | findstr :8761
taskkill /PID <PID> /F

# Linux/Mac
lsof -i :8761
kill -9 <PID>
```

### Services không đăng ký được

**Kiểm tra:**
1. Eureka server đang chạy: `http://localhost:8761`
2. Network connectivity giữa services
3. `eureka.client.service-url.defaultZone` đúng trong client config

### Self-preservation mode warning

**Nguyên nhân:** Quá nhiều service instances bị mất kết nối

**Giải pháp trong dev:**
```yaml
eureka:
  server:
    enable-self-preservation: false
```

## 📝 Logging

**Log levels:**
```yaml
logging:
  level:
    com.netflix.eureka: INFO
    com.netflix.discovery: INFO
```

**Xem logs:**
```bash
# Docker
docker logs discovery-service

# Local
tail -f logs/spring.log
```

## 🎓 Best Practices

1. **Production**: Bật self-preservation mode
2. **Load Balancing**: Deploy multiple Eureka instances
3. **Security**: Thêm Spring Security cho Eureka dashboard
4. **Monitoring**: Setup alerts cho registry size changes
5. **High Availability**: Cluster Eureka servers

## 📚 Tài liệu tham khảo

- [Spring Cloud Netflix Eureka](https://spring.io/projects/spring-cloud-netflix)
- [Eureka Wiki](https://github.com/Netflix/eureka/wiki)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)

---

**Service:** Discovery Service  
**Port:** 8761  
**Version:** 0.0.1-SNAPSHOT  
**Status:** ✅ Production Ready
