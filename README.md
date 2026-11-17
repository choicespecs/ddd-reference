# DDD Spring Boot + Vue Template

A working, domain-driven web app template (**Spring Boot 2.7 / Java 11 / JPA-Hibernate 5 / Flyway** backend and **Vue 3 / TypeScript / Vite** frontend), plus a navigable HTML guide explaining the architecture, the design choices, and how to adapt it for your own app.

## What's inside

| Path | What |
|------|------|
| `backend/` | Spring Boot app with two bounded contexts, **Catalog** and **Ordering**, each split into `domain / application / infrastructure / web` |
| `frontend/` | Vue 3 + TS app whose features mirror the backend contexts; typed API client with problem+json error handling |
| `docs/` | **The guide.** Open `docs/index.html` in a browser (18 chapters, search, diagrams, dark mode) |
| `docker-compose.yml` | PostgreSQL for the `postgres` profile |

Patterns demonstrated: aggregates and invariants, value objects (`Money`, `Sku`, typed IDs), repository ports and adapters, application services and transactions, domain events vs integration events, an anti-corruption layer between contexts, optimistic locking, Flyway-owned schema, RFC 7807/9457 problem+json errors, and ArchUnit tests that enforce the architecture.

> **Why Spring Boot 2.7?** The target is Java 11, and Spring Boot 3 requires Java 17. Boot 2.7 is the last line supporting Java 11 (its open-source support has ended, so plan an upgrade; see the guide's decision log, ADR-001). Immutable value classes use Lombok `@Value` because Java 11 has no records. The build uses `maven.compiler.release=11`, so it stays Java 11-compatible even when built on a newer JDK.

## Quick start

Requires **JDK 11+** and Node 18+.

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
