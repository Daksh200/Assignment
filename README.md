# API Gateway & Guardrails (Spring Boot)

## What this service provides
Spring Boot microservice implementing the assignment requirements:
- Phase 1: Core REST API + PostgreSQL (JPA/Hibernate entities)
- Phase 2: Redis virality score + Redis atomic guardrails (horizontal cap, cooldown, vertical cap)
- Phase 3: Notification batching + scheduled sweeper

## Local setup
### Docker
Start Postgres + Redis:
```bash
docker compose up -d
```

Default connections (see `src/main/resources/application.yml`):
- Postgres: `jdbc:postgresql://localhost:5432/assignment`
- Redis: `localhost:6379`

## Endpoints
See `postman_collection.json` in the repo root.

## Thread safety / Atomic Locks (Phase 2)
The concurrency requirement (200 concurrent bots hitting the same post in the same millisecond) must stop at exactly **100**.

### Implementation approach
Guardrails are enforced in Redis using a **single Lua script** executed via Redis, so the check + increment are atomic.

- File: `src/main/java/com/assignment/gatewayguardrails/redis/RedisGuardrailsService.java`
- Keys:
  - `post:{postId}:bot_count` — counter incremented per bot interaction
  - `cooldown:bot_{botId}:human_{humanId}` — TTL cooldown for the bot-human pair

### Atomicity guarantee
The Lua script performs these operations atomically within Redis:
1. `EXISTS cooldownKey` → reject if cooldown exists
2. `INCR botCountKey` → increments counter
3. If `newCount > maxBotCount` reject
4. If accepted, `SET cooldownKey ... EX cooldownSeconds` and return success

Because all of the above happens inside a single Redis script execution, race conditions cannot allow the counter to exceed 100.

## Notes
- PostgreSQL is the source of truth for content (posts/comments). The request is only written to Postgres after Redis guardrails allow it.
- Redis holds all counters/cooldowns/pending notifications; the app remains stateless (no Java static caches / in-memory counters).


