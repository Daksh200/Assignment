# API Gateway & Guardrails (Spring Boot)

A Spring Boot microservice that exposes a small REST API and enforces assignment “guardrails” using **Redis atomic operations**.

## Features

- **Phase 1 (Core API + Postgres)**: posts, comments, likes persisted via JPA/Hibernate
- **Phase 2 (Guardrails in Redis)**:
  - horizontal cap via atomic Lua script
  - cooldown/TTL enforcement
- **Phase 3 (Notifications)**: batching + scheduled sweeper

## Tech Stack

- Java 17
- Spring Boot 3.3.x
- Spring Web + Validation
- Spring Data JPA (PostgreSQL)
- Spring Data Redis (Redis)

## Prerequisites

- Java 17
- Docker Desktop

## Run locally (Docker)

Start Postgres + Redis:

```bash
docker compose up -d
```

Default connections (see `src/main/resources/application.yml`):

- Postgres: `jdbc:postgresql://localhost:5432/assignment`
- Redis: `localhost:6379`

### Start the app

```bash
./mvnw spring-boot:run
```

The service runs on:

- `http://localhost:8080`

## API Endpoints

See the Postman file in the repo root:

- `postman_collection.json`

Implemented endpoints:

- `POST /api/posts`
- `POST /api/posts/{postId}/comments`
- `POST /api/posts/{postId}/like`

## Guardrails / Thread Safety (Phase 2)

The concurrency requirement (e.g., **200 concurrent bots** in the same millisecond) must stop at exactly **100**.

### How it works

Guardrails are enforced in Redis using a **single Lua script** (check + increment happen atomically).

- Implementation: `src/main/java/com/assignment/gatewayguardrails/redis/RedisGuardrailsService.java`

### Atomicity guarantee

The Lua script performs all these operations atomically inside Redis:

1. Reject if cooldown key already exists
2. Atomically increment the per-post bot counter
3. Reject if the counter exceeds the cap
4. If accepted, set the cooldown key with TTL

Because all operations execute as one Redis script, race conditions cannot allow the counter to exceed the configured maximum.

## Notes

- PostgreSQL is the source of truth for content.
- Redis stores counters/cooldowns/pending notification state; the service itself stays stateless.




