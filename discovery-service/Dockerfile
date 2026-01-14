version: "3.8"

services:
  discovery-service:
    build:
      context: .
      dockerfile: Dockerfile
    image: discovery-service:latest
    container_name: discovery-service
    ports:
      - "8761:8761"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
    depends_on:
      - eureka-server

  eureka-server:
    image: eurekaserver:latest
    container_name: eureka-server
    ports:
      - "8761:8761"
    environment:
      - SPRING_PROFILES_ACTIVE=prod