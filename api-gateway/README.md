# Product Service (Port 8082)

Product Service quản lý **Product Catalog**: Products, Categories, Product Images, Product Specs.
Service này chạy trong hệ microservices và được route qua **API Gateway**.

## Tech
- Spring Boot 3.2.1, Java 17
- PostgreSQL (DB: `product_db`) + Flyway migration
- Redis cache
- RabbitMQ events
- SpringDoc OpenAPI Swagger UI

---

## Run locally (without Docker)
### 1) Prerequisites
- Java 17
- PostgreSQL 15+
- Redis 7+
- RabbitMQ 3+
- Eureka Discovery (optional for local direct run, required if using lb://)

### 2) Environment variables (recommended)
You can set via IntelliJ Run Config or OS env:

- `SPRING_PROFILES_ACTIVE=default`
- `DB_HOST=localhost`
- `DB_PORT=5432`
- `DB_NAME=product_db`
- `DB_USERNAME=postgres`
- `DB_PASSWORD=postgres`
- `REDIS_HOST=localhost`
- `REDIS_PORT=6379`
- `RABBITMQ_HOST=localhost`
- `RABBITMQ_PORT=5672`
- `EUREKA_HOST=localhost`
- `INTERNAL_TOKEN=change-me-in-prod`

### 3) Start
```bash
mvn clean package
java -jar target/product-service-*.jar
