# Chạy stack từ Docker Hub (pull & run)

Sau khi đã build và push images lên Docker Hub, bất kỳ máy nào chỉ cần **pull và chạy** mà không cần cài Java/Maven hay chỉnh config từng service.

## 1. Chuẩn bị

- Cài **Docker** và **Docker Compose**.
- Tạo file `.env` trong thư mục `Backend_Chatbot` với nội dung:

```env
DOCKERHUB_USER=<username-docker-hub-của-bạn>
```

Ví dụ: `DOCKERHUB_USER=mycompany`

## 2. Chạy toàn bộ stack

```bash
cd Backend_Chatbot
docker compose -f docker-compose.hub.yml --env-file .env up -d
```

Không cần tùy chỉnh thêm: mọi service dùng chung cấu hình mặc định (PostgreSQL `ai_agent`/`ai_agent`/`123`, Redis, RabbitMQ, Eureka hostname `discovery-service`).

## 3. Kiểm tra

- **Eureka**: http://localhost:8761
- **API Gateway**: http://localhost:8181
- **Các service**: port theo bảng trong README chính (user 8081, product 8082, ...)

## 4. Dừng

```bash
docker compose -f docker-compose.hub.yml down
```

---

## Build và push images lên Hub (cho người maintain repo)

Từ thư mục `Backend_Chatbot`:

**Linux/macOS:**
```bash
chmod +x build-push.sh
./build-push.sh <dockerhub-username> [tag]
# Ví dụ: ./build-push.sh myuser latest
```

**Windows PowerShell:**
```powershell
.\build-push.ps1 -DockerHubUser <username> -Tag latest
```

Script sẽ build từng service bằng `infrastructure/docker/Dockerfile` (multi-stage, Java 21) và push lên `username/<service-name>:tag`.

Sau khi push xong, người dùng chỉ cần set `DOCKERHUB_USER` và chạy `docker compose -f docker-compose.hub.yml up -d` như mục 2.
