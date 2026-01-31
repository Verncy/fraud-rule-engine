# Fraud Rule Engine (Java)

Spring Boot service that evaluates categorized transaction events against fraud rules, persists results, and exposes an API for retrieval.

## Tech
- Java 17
- Spring Boot
- PostgreSQL
- Flyway
- Docker Compose

## Run locally
```bash
docker compose up -d
./mvnw spring-boot:run
