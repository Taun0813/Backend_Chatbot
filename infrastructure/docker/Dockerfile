# Build stage
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copy entire project source
COPY . .

# Module name passed as build argument
ARG MODULE_NAME

# Build the specific module
# -pl: project list
# -am: also make dependents
# -DskipTests: skip tests to speed up build
RUN mvn clean package -pl ${MODULE_NAME} -am -DskipTests

# Run stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

ARG MODULE_NAME

# Copy the built jar from builder stage
# Assuming the jar is in target folder of the module
COPY --from=builder /app/${MODULE_NAME}/target/*.jar app.jar

# Expose port (optional, documented purposes mostly in docker-compose)
# EXPOSE 8080 

ENTRYPOINT ["java", "-jar", "app.jar"]
