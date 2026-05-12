# IoT Data Processing Platform — Main Application

The core backend for the IoT environmental monitoring platform. It exposes a REST API consumed by the React SPA frontend and orchestrates the domain (users, devices, rooms, measurements, alerts, automation rules, reports, AI chat) while delegating weather forecast data to the companion microservice.

## Tech Stack

| Layer            | Tooling                                                                                       |
| ---------------- | --------------------------------------------------------------------------------------------- |
| Language         | Java 17                                                                                       |
| Framework        | Spring Boot 3.4.0                                                                             |
| Web              | Spring MVC, Bean Validation, springdoc-openapi (Swagger UI)                                   |
| Persistence      | Spring Data JPA, PostgreSQL 16 (H2 in tests), HikariCP                                        |
| Security         | Spring Security, Spring OAuth2 Resource Server, custom `JwtEncoder` (HS256), refresh tokens, API-key filter for ingestion |
| Inter-service    | Spring Cloud OpenFeign → Weather Forecast microservice                                        |
| Caching          | Spring Cache + Redis (`RedisCacheManager`)                                                    |
| Reactive bits    | Spring Events (`ApplicationEventPublisher` + `@EventListener`)                                |
| Cross-cutting    | Spring AOP (`@Auditable`, `LoggingAspect`), Spring Scheduling (cron + fixed-delay)            |
| Reports          | Apache POI (Excel `.xlsx`), OpenPDF 1.4.2 (PDF, Java 17 compatible)                           |
| AI assistant     | Spring AI (OpenAI starter) — optional, gated by `app.ai.enabled`                              |
| i18n             | `MessageSource` with `en` + `bg` locales, `Accept-Language` driven                            |
| Test             | JUnit 5, Mockito, AssertJ, Spring Boot Test (`@SpringBootTest` + `MockMvc`)                   |
| Coverage gate    | JaCoCo, ≥80% line coverage (currently **~82%**)                                               |

## Modules

| Package                            | Responsibility                                                                   |
| ---------------------------------- | -------------------------------------------------------------------------------- |
| `iot.platform.user`                | Registration, profile, roles (`USER`, `ADMIN`, `INGEST`)                         |
| `iot.platform.security`            | JWT issuing/parsing, refresh tokens, API-key filter, ownership guard             |
| `iot.platform.device` / `room`     | Domain CRUD with ownership scoping                                               |
| `iot.platform.measurement`         | Single + batch ingest, paginated listing, statistics                             |
| `iot.platform.ingest`              | API-key protected batch + CSV bulk ingest endpoints used by the Raspberry Pi    |
| `iot.platform.rule`                | Automation rules + `RuleEvaluator` event listener                                |
| `iot.platform.alert`               | Alert persistence, acknowledgement                                               |
| `iot.platform.event`               | `MeasurementIngestedEvent`, `AlertRaisedEvent`                                   |
| `iot.platform.aspect`              | `@Auditable` AOP and slow-method logger                                          |
| `iot.platform.scheduling`          | Daily aggregation + retention jobs, forecast cache eviction                      |
| `iot.platform.forecast`            | Feign client + Redis-cached service wrapping the weather microservice           |
| `iot.platform.report`              | Excel + PDF exports                                                              |
| `iot.platform.ai`                  | Spring AI assistant with measurement context as RAG prompt                       |
| `iot.platform.i18n`                | `LocaleResolver` + `MessageSource` configuration                                 |

## Running locally

```bash
# 1) Start dependencies (postgres + redis) via the root docker-compose
cd .. && docker compose up -d postgres-main redis

# 2) Run the app (dev profile points to localhost:5432)
cd IoT-Data-Processing-Platform
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

The first start auto-creates the bootstrap admin (`ADMIN_USERNAME`/`ADMIN_PASSWORD` env vars, defaults `admin/ChangeMeAdmin123!`).

Swagger UI: <http://localhost:8080/swagger-ui/index.html>
Actuator health: <http://localhost:8080/actuator/health>

## Running with Docker

```bash
cd ..
cp .env.example .env  # tweak secrets
docker compose up --build
```

Then visit:

- Frontend SPA — <http://localhost>
- Main API + Swagger — <http://localhost:8080/swagger-ui/index.html>
- Weather API + Swagger — <http://localhost:8081/swagger-ui/index.html>

## Tests + coverage

```bash
./mvnw verify        # runs unit + integration tests and enforces the JaCoCo gate
open target/site/jacoco/index.html
```

Coverage exclusions (integration-bound, not unit-testable): `ai.*`, `scheduling.*`, `AdminBootstrap`, JWT auth entrypoints, generated config.

## API surface (high level)

| Method | Path                                         | Notes                                |
| ------ | -------------------------------------------- | ------------------------------------ |
| POST   | `/api/auth/register`                         | Public                               |
| POST   | `/api/auth/login`                            | Returns JWT + refresh                |
| POST   | `/api/auth/refresh`                          |                                      |
| GET    | `/api/users/me`                              | Auth                                 |
| PUT    | `/api/users/me`                              | Profile update                       |
| GET    | `/api/admin/users` …                         | Admin only                           |
| CRUD   | `/api/rooms`, `/api/devices`, `/api/rules`   | Owner-scoped                         |
| POST   | `/api/ingest/measurements`                   | API-key                              |
| POST   | `/api/ingest/measurements/csv`               | API-key, multipart                   |
| GET    | `/api/measurements`                          | Filter by device + window            |
| GET    | `/api/alerts` / POST `/api/alerts/{id}/ack`  |                                      |
| GET    | `/api/forecast/{lat},{lon}`                  | Redis-cached, Feign → microservice   |
| GET    | `/api/reports/devices/{id}/measurements`     | `?format=EXCEL` or `PDF`             |
| POST   | `/api/ai/chat`                               | Requires `AI_ENABLED=true`           |

## Configuration knobs

See `src/main/resources/application.yml`. Common env vars:

| Variable             | Default                  | Purpose                                       |
| -------------------- | ------------------------ | --------------------------------------------- |
| `DB_USERNAME`        | `postgres`               | Postgres user                                 |
| `DB_PASSWORD`        | `postgres`               | Postgres password                             |
| `JWT_SECRET`         | dev string               | HS256 signing key (≥32 chars)                 |
| `CORS_ORIGINS`       | `http://localhost:5173`  | Comma-separated allowed origins               |
| `FORECAST_SERVICE_URL` | `http://localhost:8081` | Microservice base URL                         |
| `AI_ENABLED`         | `false`                  | Enables `/api/ai/chat`                        |
| `OPENAI_API_KEY`     | -                        | Required when AI enabled                      |

## Project layout

```
src/main/java/iot/platform/
├── IotPlatformApplication.java
├── ai/                # Spring AI assistant
├── alert/             # Alert domain
├── aspect/            # @Auditable, LoggingAspect
├── config/            # Beans (Redis, Feign, OpenAPI, …)
├── device/, room/, measurement/, rule/
├── event/             # Spring Events
├── exception/         # Custom exceptions + @RestControllerAdvice
├── forecast/          # Feign client + service + controller
├── i18n/              # MessageSource + LocaleResolver
├── ingest/            # Raspberry-Pi facing ingest
├── report/            # Excel + PDF export
├── scheduling/        # Cron + fixedDelay jobs
├── security/          # JWT, API key, ownership guard
└── user/              # Registration, profile, admin
```
