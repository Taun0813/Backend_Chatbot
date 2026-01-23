FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 8761

ENV SPRING_PROFILES_ACTIVE=docker

ENTRYPOINT ["java", "-Xmx512m", "-Xms256m", "-jar", "app.jar"]
