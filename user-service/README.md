# User Service

## Overview
User Service handles authentication, authorization, and user management for the E-commerce system.

## Key Features
- **Authentication**: Register, Login, Refresh Token (JWT).
- **Security**: 
  - Standard Spring Security for internal endpoints.
  - Integration with API Gateway (trusts `X-User-*` headers).
  - JWT generation and validation.
- **Database**: PostgreSQL with Flyway migration.
- **Monitoring**: Actuator & Prometheus metrics.
- **Documentation**: OpenAPI/Swagger UI.

## API Documentation
Swagger UI is available at:
- **Direct**: [http://localhost:8081/swagger-ui/index.html](http://localhost:8081/swagger-ui/index.html)
- **Via Gateway**: [http://localhost:8080/webjars/swagger-ui/index.html?urls.primaryName=user-service](http://localhost:8080/webjars/swagger-ui/index.html?urls.primaryName=user-service) (requires Gateway Swagger aggregation config, or direct access via Gateway routing)

## Configuration
See `application.yml` and `application-docker.yml` for details.

### Environment Variables
| Variable | Description | Default |
|----------|-------------|---------|
| `JWT_SECRET` | Secret key for signing JWTs | (dev default provided) |
| `SPRING_DATASOURCE_URL` | DB URL | `jdbc:postgresql://localhost:5432/ai_agent_db` |
| `SPRING_DATASOURCE_USERNAME` | DB Username | `ai_agent` |
| `SPRING_DATASOURCE_PASSWORD` | DB Password | `123` |

## Docker
Build and run with creating the image:
```bash
docker build -t user-service .
```

## Testing
### Register
```bash
curl -X POST http://localhost:8081/auth/register \
-H "Content-Type: application/json" \
-d '{"email": "test@example.com", "password": "password", "fullName": "Test User"}'
```

### Login
```bash
curl -X POST http://localhost:8081/auth/login \
-H "Content-Type: application/json" \
-d '{"email": "test@example.com", "password": "password"}'
```
