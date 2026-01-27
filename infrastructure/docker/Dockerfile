# syntax=docker/dockerfile:1

# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy parent pom and caching dependencies
COPY pom.xml .
COPY discovery-service/pom.xml discovery-service/
COPY api-gateway/pom.xml api-gateway/
COPY product-service/pom.xml product-service/
COPY user-service/pom.xml user-service/
COPY order-service/pom.xml order-service/
COPY payment-service/pom.xml payment-service/
COPY warranty-service/pom.xml warranty-service/
COPY recommendation-service/pom.xml recommendation-service/
COPY cart-service/pom.xml cart-service/
COPY inventory-service/pom.xml inventory-service/

# Download dependencies (this layer will be cached if poms don't change)
RUN mvn dependency:go-offline -B

# Copy source code
COPY . .

# Build the specific module passed as argument
ARG MODULE_NAME
RUN mvn clean package -pl ${MODULE_NAME} -am -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
ARG MODULE_NAME
COPY --from=build /app/${MODULE_NAME}/target/*.jar app.jar

# Expose port (can be overridden at runtime, but good for documentation)
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
