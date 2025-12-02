# Document Management System (DMS)

A backend service that manages documents, branches, version history, permissions, and real-time collaboration.

Built with **Spring Boot**, secured with **Spring Security**, backed by **MySQL**, optimized with **Redis caching**,
containerized with **Docker**, and supports real-time updates via **WebSockets**.

---

## Features

### User Authentication & Authorization

- JWT-based security with a role/permission model
- Static permission checks for branches and document updates

### Document & Branch Management

- Create, update, version, and restore documents
- Branching workflow similar to Git, optimized for text content

### Real-Time Collaboration

- WebSocket channels for live document updates

### Caching Layer

Redis used for:

- Cached frequently accessed documents
- Rate-limiting

### Database Persistence

- MySQL for durable storage
- Designed with normalization + optimized indexes

### Containerized Infrastructure

- Docker Compose environment for MySQL, Redis, and the backend

---

## Tech Stack

| Layer            | Technologies                                 |
|------------------|----------------------------------------------|
| Backend          | Spring Boot 3.x, Spring Web, Spring Data JPA |
| Security         | Spring Security, JWT                         |
| Database         | MySQL 8.0.44+                                |
| Cache            | Redis                                        |
| Messaging        | WebSockets (STOMP)                           |
| Containerization | Docker & Docker Compose                      |
| Testing          | JUnit 5, Mockito                             |

---

## Project Structure

```
src/main/java/com/nipun/system
├── auth/            # Login, JWT issuance, authentication flow
├── user/            # Users, roles, profile, websocket presence
├── document/
│   ├── base/        # Core document model, CRUD, repository
│   ├── branch/      # Branching workflow (Git-like)
│   ├── version/     # Version history
│   ├── diff/        # Text diff/patch utilities
│   ├── permission/  # Access control and permission rules
│   ├── share/       # Document sharing logic
│   └── trash/       # Soft-deletion logic
├── filemanager/     # File uploads
├── shared/
│   ├── config/      # Security, Redis, WebSockets, OpenAPI
│   ├── exceptions/  # Global exception handlers
│   ├── utils/       # Common utilities
│   └── services/    # Shared cross-module services
└── websocket/       # STOMP channels, events, interceptors

```

---

## Running the Project

### Prerequisites

- Docker & Docker Compose
- JDK 24+
- Maven 3.9+

### 1. Start Infrastructure (MySQL + Redis)

```bash
docker compose up -d
```

This runs:

- `mysql:8`
- `redis:8.2.1`
- Any other services defined in `docker-compose.yml`

### 2. Start Spring Boot Backend

```bash
./mvnw spring-boot:run
```

Or build and run:

```bash
./mvnw clean package
java -jar target/document-management-system.jar
```

## Environment Variables

| Variable                       | Description                                   |
|--------------------------------|-----------------------------------------------|
| `MYSQL_HOST`                   | MySQL database host address                   |
| `MYSQL_PORT`                   | MySQL database port                           |
| `MYSQL_ROOT_PASSWORD`          | MySQL root user password                      |
| `MYSQL_DATABASE`               | MySQL database name                           |
| `MYSQL_USER`                   | MySQL application user                        |
| `MYSQL_PASSWORD`               | MySQL application user password               |
| `JWT_SECRET_KEY`               | Secret key for signing JWTs                   |
| `JWT_ACCESS_TOKEN_EXPIRATION`  | Access token expiration time (e.g., 15m, 1h)  |
| `JWT_REFRESH_TOKEN_EXPIRATION` | Refresh token expiration time (e.g., 7d, 30d) |
| `CLIENT_URL`                   | Frontend application URL for CORS             |
| `AZURE_BLOB_SERVICE_ENDPOINT`  | Azure Blob Storage service endpoint           |
| `AZURE_BLOB_SERVICE_SAS_TOKEN` | Azure Blob Storage SAS token                  |
| `AZURE_BLOB_SERVICE_CONTAINER` | Azure Blob Storage container name             |

---

## Security Overview

- JWT authentication with an access token and optional refresh token
- Passwords hashed using BCrypt
- Permission rules:
    - **Unauthorized** → cannot view a document
    - **Read-only** → can view but not modify
    - **Read-write** → full access

---

## WebSocket Endpoints

| Endpoint               | Description                      |
|------------------------|----------------------------------|
| `/ws`                  | WebSocket handshake              |
| `/topic/document/{id}` | Broadcast updates to subscribers |
| `/app/document.update` | Client-to-server updates         |

**Protocol:** STOMP over WebSocket

---

## Testing

Run unit tests:

```bash
./mvnw test
```

The project uses:

- JUnit 5
- Mockito (including static mocking)
- Testcontainers (optional) for MySQL/Redis integration tests

---

## Database Schema

- **Documents**
- **Branches**
- **Content**
- **Version history**
- **Users & permissions**
- **Templates**
- **Comments**
- **Shared Documents**

---

## Docker Compose (example)

```yaml
services:
  mysql:
    image: mysql:8
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: dms
    ports:
      - "3306:3306"

  redis:
    image: redis:latest
    ports:
      - "6379:6379"
```

---

## API Documentation

Swagger/OpenAPI is available at:

- `/swagger-ui.html`
- `/v3/api-docs`

---

## Roadmap

- Collaborative edit conflict resolution
- Offline syncing
- S3 file attachments
- RBAC admin dashboard
- Document metadata search (Elasticsearch optional)

---