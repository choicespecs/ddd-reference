# DDD Spring Boot + Vue Template

A working, domain-driven web app template (**Spring Boot 3 / Java 17 / JPA-Hibernate / Flyway** backend and **Vue 3 / TypeScript / Vite** frontend), plus a navigable HTML guide explaining the architecture, the design choices, and how to adapt it for your own app.

## What's inside

| Path | What |
|------|------|
| `backend/` | Spring Boot app with two bounded contexts, **Catalog** and **Ordering**, each split into `domain / application / infrastructure / web` |
| `frontend/` | Vue 3 + TS app whose features mirror the backend contexts; typed API client with ProblemDetail error handling |
| `docs/` | **The guide.** Open `docs/index.html` in a browser (18 chapters, search, diagrams, dark mode) |
| `docker-compose.yml` | PostgreSQL for the `postgres` profile |

Patterns demonstrated: aggregates and invariants, value objects (`Money`, `Sku`, typed IDs), repository ports and adapters, application services and transactions, domain events vs integration events, an anti-corruption layer between contexts, optimistic locking, Flyway-owned schema, RFC 9457 errors, and ArchUnit tests that enforce the architecture.

## Quick start

```bash
# backend → http://localhost:8080  (in-memory H2, seeded demo data)
cd backend && ./mvnw spring-boot:run

# frontend → http://localhost:5173  (proxies /api to :8080)
cd frontend && npm install && npm run dev
```

- Swagger UI: http://localhost:8080/swagger-ui.html
- H2 console: http://localhost:8080/h2-console (JDBC URL `jdbc:h2:mem:shop`, user `sa`)
- PostgreSQL instead of H2: `docker compose up -d`, then `./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres`

## Tests

```bash
cd backend && ./mvnw test     # domain unit tests, ArchUnit rules, full API flow test
cd frontend && npm run build  # type-check + production build
```

## The guide

Open **`docs/index.html`** directly (no server needed). Suggested reading order: *Start here → DB to screen → DDD fundamentals → Strategic design → Tactical patterns → …*

To edit or add pages: chapter sources live in `docs/_chapters/*.html` (HTML fragments with triple-backtick code fences). Rebuild with:

```bash
python3 docs/build.py
```
