# User Service

## Overview
The **User Service** is a core microservice responsible for user authentication, registration, profile management, and address handling. It serves as the identity provider for the entire system, issuing JWT tokens for secure access.

## Tech Stack
- **Java 17**
- **Spring Boot 3.2.1**
- **Spring Security** (JWT Authentication)
- **Spring Data JPA** (Hibernate 6)
- **PostgreSQL** (Database)
- **Redis** (Caching & Token Blacklisting)
- **Flyway** (Database Migration)
- **RabbitMQ** (Event Messaging)
- **Eureka Client** (Service Discovery)
- **Testcontainers** (Integration Testing)

## Features
- **Authentication**: Login, Logout, Refresh Token (JWT).
- **Registration**: New user sign-up with role assignment.
- **Profile Management**: View and update user details.
- **Address Management**: CRUD operations for user addresses.
- **Role-Based Access Control (RBAC)**: Supports `ROLE_SUPER_ADMIN`, `ROLE_ADMIN`, `ROLE_USER`.
- **Security**:
    - Stateless authentication using JWT.
    - Header-based authentication (`X-User-Id`, `X-User-Roles`) for internal service communication.
- **Observability**:
    - Distributed Tracing via `X-Request-Id` (MDC).
    - Actuator endpoints for health checks and metrics.

## API Documentation
Swagger UI is available at:
`http://localhost:8085/swagger-ui/index.html`

### Key Endpoints
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/users/auth/register` | Register a new user | No |
| POST | `/users/auth/login` | Login and get tokens | No |
| POST | `/users/auth/refresh-token` | Refresh access token | No |
| POST | `/users/auth/logout` | Logout user | Yes |
| GET | `/users/me` | Get current user profile | Yes |
| PUT | `/users/me` | Update current user profile | Yes |
| GET | `/users/me/addresses` | Get user addresses | Yes |
| POST | `/users/me/addresses` | Add new address | Yes |

## Configuration
### Environment Variables / `application.yml`
| Variable | Default | Description |
|----------|---------|-------------|
| `server.port` | `8085` | Service port |
| `spring.datasource.url` | `jdbc:postgresql://localhost:5433/ai_agent` | Database URL |
| `spring.data.redis.host` | `localhost` | Redis host |
| `eureka.client.service-url.defaultZone` | `http://localhost:8761/eureka/` | Eureka Server URL |
| `jwt.secret` | *(Secret Key)* | JWT signing key |

## Running the Service

### Prerequisites
- Java 17+
- Maven
- Docker (for dependencies)

### 1. Start Infrastructure
Use Docker Compose to start PostgreSQL, Redis, RabbitMQ, and Eureka:
```bash
docker-compose up -d postgresql redis rabbitmq ai-agent-discovery
```

### 2. Run with Maven
```bash
mvn spring-boot:run
```

### 3. Run Tests
Unit and Integration tests use **Testcontainers**:
```bash
mvn test
```

## Database Migration
Flyway is enabled and will automatically migrate the database schema on startup.
Migration scripts are located in `src/main/resources/db/migration`.

## Docker
Build the Docker image:
```bash
mvn clean package -DskipTests
docker build -t user-service .
```
