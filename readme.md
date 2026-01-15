# Document Management System (DMS)

Backend service for managing documents, branches, versions, permissions, and real‑time collaboration.

Built with Spring Boot, secured by Spring Security (JWT), backed by MySQL, cached with Redis, containerized with Docker,
and supporting real‑time updates over WebSockets.

---

## Overview

- Documents with branching and version history
- Permission model with read/write access checks
- Real‑time user presence and updates over STOMP/WebSockets
- Flyway database migrations
- OpenAPI/Swagger UI
- Caching and basic rate limiting

---

## Tech Stack

- Language/runtime: Java 24
- Frameworks: Spring Boot 3.5.x (Web, Data JPA, Security, WebSocket, Actuator)
- Database: MySQL 8
- Cache: Redis
- Migrations: Flyway
- Auth: JWT (jjwt)
- Mapping: MapStruct
- API Docs: springdoc-openapi
- Resilience: resilience4j (rate limiter)
- Build/PM: Maven (mvnw wrapper)
- Containers: Docker & Docker Compose
- Tests: JUnit 5, Spring Boot Test, Mockito

---

## ER Diagram

![ER Diagram](/resources/er.png)

## Project Structure

```
src/main/java/com/nipun/system
├── document/
│   ├── base/        # Core document model, CRUD, repository
│   ├── branch/      # Branching workflow and diffs
│   ├── version/     # Version history
│   ├── diff/        # Text diff utilities
│   ├── permission/  # Access control and permission rules
│   └── ...
├── shared/
│   ├── config/      # Security, Redis, WebSockets, OpenAPI
│   ├── exceptions/  # Global exception handlers
│   └── utils/       # Common utilities
└── user/websocket   # Presence, listeners, services

src/main/resources
├── application.yml   # Spring configuration (DB, Redis, JWT, OpenAPI, etc.)
└── db/migration      # Flyway migrations
```

---

## Requirements

- Docker & Docker Compose
- JDK 24+
- Maven 3.9+ (or use `./mvnw`)

---

## Setup and Run

There are two common workflows: all-in-Docker, or local app with Dockerized services.

### A. Run everything via Docker Compose

1) Start services and app

```bash
docker compose -f compose.yml up -d
```

This will:

- Build and run the server container (port 8080 → host 8080)
- Start MySQL 8 (exposed as host 3307 → container 3306)
- Start Redis 8.2.1 (6379)

Spring profile: `dev` (see `SPRING_PROFILES_ACTIVE` in `compose.yml`).

2) Stop

```bash
docker compose down
```

### B. Run app locally with Dockerized MySQL/Redis

1) Start infra only

```bash
docker compose up -d
```

2) Export environment for local run

```bash
export MYSQL_HOST=localhost
export MYSQL_PORT=3307
# Also export: MYSQL_DATABASE, MYSQL_USER, MYSQL_PASSWORD, JWT_SECRET_KEY, ... (see Env Vars below)
```

3) Run the app

```bash
./mvnw spring-boot:run
```

### Build a jar and run

```bash
./mvnw clean package
java -jar target/document-management-system-0.0.1-SNAPSHOT.jar
```

### Production (example)

- Dockerfile: `Dockerfile.prod`
- Compose: `compose.prod.yml`

Example (adjust to your registry and env):

```bash
docker build -f Dockerfile.prod -t dms:prod .
docker compose -f compose.prod.yml up -d
```

---

## Environment Variables

Defined/used in `src/main/resources/application.yml` and compose files:

| Variable                       | Required | Notes                                                    |
|--------------------------------|----------|----------------------------------------------------------|
| `MYSQL_HOST`                   | yes      | e.g., `mysql` (inside Docker) or `localhost` (local run) |
| `MYSQL_PORT`                   | yes      | 3306 in container; 3307 on host via compose mapping      |
| `MYSQL_DATABASE`               | yes      | Database name                                            |
| `MYSQL_USER`                   | yes      | Application DB user                                      |
| `MYSQL_PASSWORD`               | yes      | Application DB password                                  |
| `MYSQL_ROOT_PASSWORD`          | optional | Used by MySQL container initialization                   |
| `JWT_SECRET_KEY`               | yes      | Secret for signing JWTs                                  |
| `JWT_ACCESS_TOKEN_EXPIRATION`  | yes      | e.g., `15m`, `1h`                                        |
| `JWT_REFRESH_TOKEN_EXPIRATION` | yes      | e.g., `7d`, `30d`                                        |
| `CLIENT_URL`                   | optional | Frontend origin for CORS                                 |
| `AZURE_BLOB_SERVICE_ENDPOINT`  | optional | Azure Blob endpoint                                      |
| `AZURE_BLOB_SERVICE_SAS_TOKEN` | optional | Azure Blob SAS token                                     |
| `AZURE_BLOB_SERVICE_CONTAINER` | optional | Azure container name                                     |

---

## WebSockets

- STOMP over WebSocket
- Handshake endpoint (from `WebSocketConfig`):

```
/gs-guide-websocket
```

- Broker destinations enabled: `/document`, `/queue/`, `/user/`
- Application destination prefix: `/app`

Examples:

- Subscribe: `/document/{some-topic}` or `/user/{userId}/status`
- Send: `/app/{mapping}` (depends on controller mappings)

TODO: Document specific application destinations and message payloads.

---

## API and Docs

- Swagger UI (springdoc):
    - `http://localhost:8080/swagger-ui/index.html`
    - OpenAPI JSON: `/v3/api-docs`
- Actuator (if enabled by profile/config): `/actuator`

---

## Database & Migrations

- Flyway runs on startup; place migrations under `src/main/resources/db/migration`
- DataSource configured via `MYSQL_*` variables

---

## Caching & Rate Limiting

- Redis connection is configured in `application.yml`
- Basic rate limiter configured via `resilience4j` (`resilience4j.ratelimiter.*`)

---

## Scripts and Useful Commands

- Start everything (dev): `docker compose up -d`
- Start only DB/Cache: `docker compose up -d mysql redis`
- Run app locally: `./mvnw spring-boot:run`
- Run tests: `./mvnw test`
- Format package and run: `./mvnw clean package && java -jar target/document-management-system-0.0.1-SNAPSHOT.jar`

---

## Testing

Run all tests:

```bash
./mvnw test
```

Run a single test:

```bash
./mvnw -Dtest=com.nipun.system.document.version.VersionServiceTest test
```

The project uses JUnit 5, Spring Boot Test, Mockito. No Testcontainers dependency is declared by default.

---

## License

This project is licensed under the Apache License, Version 2.0.  
See the [LICENSE](LICENSE) file for details.

---

## Notes

- Default server port: `8080`
- Default profile in Docker: `dev` (via `SPRING_PROFILES_ACTIVE`)
- MySQL host/port when running locally: `localhost:3307` (mapped from the container)