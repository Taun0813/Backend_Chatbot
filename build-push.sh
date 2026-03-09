#!/usr/bin/env bash
# Build và push tất cả service lên Docker Hub
# Cách dùng: ./build-push.sh <dockerhub-username> [tag]
# Ví dụ: ./build-push.sh myuser latest

set -e
DOCKERHUB_USER="${1:?Usage: $0 <dockerhub-username> [tag]}"
TAG="${2:-latest}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

MODULES=(
  "discovery-service:8761"
  "api-gateway:8181"
  "user-service:8081"
  "product-service:8082"
  "order-service:8084"
  "payment-service:8085"
  "cart-service:8086"
  "inventory-service:8083"
  "warranty-service:8088"
  "recommendation-service:8089"
)

for entry in "${MODULES[@]}"; do
  MODULE="${entry%%:*}"
  PORT="${entry##*:}"
  IMAGE="${DOCKERHUB_USER}/${MODULE}:${TAG}"
  echo "Building ${MODULE} (port ${PORT}) -> ${IMAGE}"
  docker build -f infrastructure/docker/Dockerfile \
    --build-arg MODULE="${MODULE}" \
    --build-arg APP_PORT="${PORT}" \
    -t "${IMAGE}" .
  echo "Pushing ${IMAGE}"
  docker push "${IMAGE}"
done

echo "Done. Chạy stack: docker compose -f docker-compose.hub.yml --env-file .env up -d"
