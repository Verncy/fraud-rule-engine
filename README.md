# Fraud Rule Engine (Java)

Spring Boot service that evaluates categorized transaction events against fraud rules, persists results, and exposes an API for retrieval.

This project supports two execution modes:

- **Path A – Local development:** Run Postgres via Docker, app via Maven
- **Path B – Full Docker:** Run both database and app via Docker Compose


---

## Tech

- Java 17  
- Spring Boot  
- PostgreSQL  
- Flyway  
- Docker Compose  

---

## Run via Docker

This project uses Docker Compose for PostgreSQL and a Dockerfile for the application.

### 1. Start the stack

From the project root:

```powershell
docker compose up --build -d
```

This starts:

- fraudruleengine-app (Spring Boot API)

- fraudruleengine-db (PostgreSQL)

Database Details:

- Database: fraud

- Username: fraud

- Password: fraud

- Port: 5432

API is exposed on:

```
http://localhost:8081
```

### 2. Verify Health

```
curl.exe http://localhost:8081/actuator/health
```

Expected:

```
{"status":"UP"}
```

### 3. Evaluate a transaction

```
curl.exe -i -X POST "http://localhost:8081/v1/transactions/evaluate" `
  -H "Content-Type: application/json" `
  --data-binary "@request.json"
```


### 4. View Logs:

```
docker compose logs -f app

```

### 5. Stop / cleanup

```
docker compose down
```

---

## Run locally

```
docker compose up -d db
./mvnw spring-boot:run
```

Service starts at:

```
http://localhost:8080
```
(Note: Docker mode exposes the API on port 8081; local mode uses 8080.)


---

## Architecture Overview

This service follows a layered architecture:

- API layer receives transaction events
- Domain layer evaluates fraud rules
- Persistence layer stores transactions, cases, and rule hits
- PostgreSQL is used for durable storage
- Flyway manages schema migrations

Flow:

Client  
→ REST Controller  
→ FraudEvaluationService  
→ Rules Engine  
→ PostgreSQL  

Each transaction is persisted first, then evaluated.  
Rule hits and fraud cases are stored atomically in a single transaction.

---

## Project Structure

```
src/main/java/com/example/FraudRuleEngine
├── api          # REST controllers + DTOs
├── config       # Rule + Jackson configuration
├── domain       # Core fraud models and rules
├── persistence  # JPA entities + repositories
└── service      # FraudEvaluationService orchestration
```

---

## Rules Configuration

Rules are wired via Spring configuration in `RulesConfig`:

- HighAmountRule (threshold: 50,000)
- MerchantWatchlistRule (ACME, BINANCE)
- VelocityRule (5 transactions in 2 minutes)

Rules are injected as a list and evaluated sequentially.

New rules can be added by:

1. Implementing `FraudRule`
2. Registering in `RulesConfig`

---

## Fraud Scoring & Rule Evaluation

### Rule evaluation

Each rule may return a `RuleHit`.  
All hits are collected and persisted.

### Risk scoring

Risk score is calculated from severities:

- LOW = 10 points  
- MEDIUM = 30 points  
- HIGH = 70 points  

Final score = sum of triggered severities, capped at 100.

### Flagging logic

A transaction is flagged when:

- Any HIGH severity rule triggers  
- OR total risk score ≥ 70  

---

## Velocity Rule

The velocity rule detects bursts of activity per customer.

Logic:

- Counts transactions for the same customer
- Within a rolling time window
- Current transaction is included (already persisted)

Trigger condition:

```
count > maxCount
```

This avoids off-by-one errors.

Example:

Window: 2 minutes  
Max: 5  

Trigger occurs on the 6th transaction.

Velocity uses database counting instead of in-memory state to remain stateless and horizontally scalable.

---

## Persistence Model

Tables:

- `transactions` – raw events + payload (JSONB)
- `fraud_cases` – one per transaction
- `rule_hits` – one per triggered rule

Relationships:

```
transactions → fraud_cases → rule_hits
```

Foreign keys use `ON DELETE CASCADE` to allow clean test resets.

JSONB is used for:

- raw transaction payload
- rule metadata

This enables auditability and debugging.

---

## Validation & Errors

Incoming requests are validated using Bean Validation (jakarta.validation).

Validation rules

The request DTO applies constraints such as:

- @NotBlank for required IDs (transactionId, customerId)

- @NotNull for required fields (amount, eventTime)

- @Positive for amount

- @Size limits for strings (IDs, merchant, category, currency)

Validation is enforced on the evaluate endpoint via @Valid.

Error responses (HTTP 400)

If validation fails, the API returns HTTP 400 Bad Request with a JSON response that includes:

- a general message

- a list of field-level errors

Example:

{
  "timestamp": "2026-02-03T18:52:58Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": [
    "transactionId: transactionId is required",
    "amount: amount must be greater than 0"
  ]
}

Notes:

- The errors array contains one entry per invalid field.

- The exact timestamp format may vary.

---

## Demo

A demo PowerShell script is included:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\demo.ps1
```

This runs three scenarios:

### 1. High Amount

Amount exceeds configured threshold.

Triggers `HIGH_AMOUNT`.

Transaction is flagged.

---

### 2. Watchlist Merchant

Merchant is on watchlist (ACME / BINANCE).

Triggers `MERCHANT_WATCHLIST`.

Medium risk score applied.

---

### 3. Velocity Burst

Sends 6 transactions rapidly for the same customer.

Velocity allows 5 within the window.

6th transaction triggers `VELOCITY`.

---

Expected results:

- High amount → flagged (HIGH severity)
- Watchlist → medium score
- Velocity → triggers on 6th transaction

---

## Reset Database

To clear demo data while keeping schema:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\reset-db.ps1
```

This truncates:

- rule_hits
- fraud_cases
- transactions

Foreign keys use cascade so cleanup is safe.

---

## Testing

Currently:

- Manual demo via PowerShell script

Planned:

- Unit tests per rule
- Integration tests with Testcontainers
- API contract tests

---

## Stop Environment

```bash
docker compose down
```

---

## Design Decisions

- Transactions are persisted before rule evaluation to support velocity counting.
- JSONB is used for raw payloads and rule metadata for auditability.
- Rules use interface abstraction for extensibility.
- Scoring is additive and capped for simplicity and explainability.
- Velocity is database-backed instead of in-memory to remain stateless.
- Foreign keys use cascade for clean test resets.

---

## Future Improvements

- Async ingestion via Kafka
- Redis-based velocity counters
- Rule versioning + activation flags
- Admin UI for managing thresholds
- Idempotency keys
- JWT authentication
- Metrics per rule
- Rule execution tracing

---
