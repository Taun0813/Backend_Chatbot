# Dockerfile chung cho mọi service - build với: docker build --build-arg MODULE=<tên-module> --build-arg APP_PORT=<port> -t <image-name> -f infrastructure/docker/Dockerfile .
# Ví dụ: docker build --build-arg MODULE=discovery-service --build-arg APP_PORT=8761 -t myuser/discovery-service:latest -f infrastructure/docker/Dockerfile .

ARG MODULE=discovery-service
ARG APP_PORT=8761

# --- Build stage ---
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
ARG MODULE
WORKDIR /build

# Copy parent POM và toàn bộ modules (Maven reactor)
COPY pom.xml .
COPY discovery-service discovery-service
COPY api-gateway api-gateway
COPY user-service user-service
COPY product-service product-service
COPY order-service order-service
COPY payment-service payment-service
COPY warranty-service warranty-service
COPY recommendation-service recommendation-service
COPY cart-service cart-service
COPY inventory-service inventory-service

# Chỉ build module được chỉ định và dependencies
RUN mvn -B -pl ${MODULE} -am -DskipTests package && \
    cp ${MODULE}/target/*.jar app.jar

# --- Runtime stage ---
FROM eclipse-temurin:21-jre-alpine
ARG APP_PORT
RUN apk add --no-cache tzdata curl && adduser -D -s /bin/sh appuser
WORKDIR /app

COPY --from=builder /build/app.jar app.jar
RUN chown -R appuser:appuser /app
USER appuser

EXPOSE ${APP_PORT}
ENTRYPOINT ["java", "-jar", "app.jar"]
