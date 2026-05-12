# IoT Data Processing Platform — Main Application

The core backend system for the IoT environmental monitoring platform. It exposes a REST API consumed by the React SPA frontend and orchestrates the domain (devices, rooms, measurements, alerts, automation rules) while delegating weather forecast data to the companion microservice.

## Tech Stack

- Java 17, Spring Boot 3.4.0
- Spring Web, Spring Data JPA, Spring Security (JWT via `spring-boot-starter-oauth2-resource-server`)
- Spring Cloud OpenFeign (calls the weather microservice)
- Spring Data Redis + Spring Cache (Redis-backed caching)
- Spring AOP, Spring Events, Spring Scheduling
- Spring AI (OpenAI starter) — optional, gated by `app.ai.enabled`
- Apache POI (Excel export) + OpenPDF (PDF export)
- PostgreSQL 16, H2 (tests)
- springdoc-openapi (Swagger UI)
- JaCoCo (≥80% line coverage gate)

## Features

(filled in as phases land)

## Running

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

See `../docker-compose.yml` for the full stack.
