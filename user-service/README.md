# User Service

## Overview
User management service handling authentication, registration, profile management, and role-based access control.

## Key Features
- **Authentication**: JWT-based login, registration, and token refresh.
- **Microservice Integration**: Registers with Eureka, communicates via RabbitMQ (Event-driven).
- **Security**: Role-based access control (USER, ADMIN, SUPER_ADMIN).
- **Database**: PostgreSQL for user data and profiles.

## API Documentation
Swagger UI: `http://localhost:8081/swagger-ui.html`

## Running Locally
```bash
mvn spring-boot:run
```

## Docker
```bash
docker build -t user-service .
docker run -p 8081:8081 user-service
```
