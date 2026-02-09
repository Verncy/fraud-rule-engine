# Architectural Decisions

This document captures key design decisions made for the Fraud Rule Engine take-home and the reasoning behind them.

## 1) Layered architecture (api / domain / service / persistence)

**Decision:** Use a layered structure:
- **api**: controllers + DTOs
- **domain**: fraud rules + core models
- **service**: orchestration and transaction boundary
- **persistence**: JPA entities + repositories

**Why:** Keeps responsibilities clear, supports testability, and mirrors real-world Spring Boot service structure.

## 2) Persist transaction before evaluation

**Decision:** Persist the transaction event before applying fraud rules.

**Why:**
- Enables database-backed rules (e.g., velocity) to include the current transaction reliably.
- Provides an audit trail (raw payload + attributes) even if later rule logic changes.

**Trade-off:** Slightly more writes; accepted for correctness and auditability.

## 3) Rules as pluggable components

**Decision:** Model each fraud rule as a `FraudRule` implementation that optionally returns a `RuleHit`.

**Why:**
- New rules can be added without changing the evaluation flow.
- Encourages single-responsibility per rule and clean extensibility.

## 4) Risk scoring model: additive, capped

**Decision:** Calculate risk score by summing severity weights (LOW/MEDIUM/HIGH) and cap to 100.

**Why:**
- Easy to explain and reason about.
- Deterministic and stable for a take-home submission.

**Trade-off:** Not statistically calibrated; a real system would tune weights using historical data.

## 5) Flagging logic: HIGH or riskScore ≥ threshold

**Decision:** Flag if any HIGH severity rule triggers OR risk score is above threshold.

**Why:** Combines “hard-stop” rules with a simple aggregate scoring approach.

## 6) Store raw payload and rule metadata as JSON

**Decision:** Store the original request payload and rule metadata (when present) as JSON.

**Why:**
- Auditability and debugging (what did the system actually see?)
- Flexible schema for per-rule metadata without frequent migrations.

## 7) Monitoring via domain event + AFTER_COMMIT listener

**Decision:** When a transaction is flagged, publish a `FraudFlaggedEvent`.
Consume it using `@TransactionalEventListener(phase = AFTER_COMMIT)` and send to the monitoring provider.

**Why:**
- Ensures alerts are only emitted once the DB transaction is committed (no “false alerts” for rolled-back work).
- Decouples core fraud evaluation from external integrations (webhook/Slack/PagerDuty).
- Prevents external failures from rolling back fraud evaluation.

**Trade-off:** Monitoring becomes asynchronous relative to the request (intentional).

## 8) Webhook.site for demo; provider abstraction for production

**Decision:** Use WEBHOOK (webhook.site) for the take-home demo, but keep an abstraction that supports Slack and PagerDuty.

**Why:**
- Demo works without a corporate PagerDuty account.
- Shows production intent: provider can be swapped via environment variables without touching domain logic.

## 9) Stateless service design for horizontal scaling

**Decision:** Keep the application stateless. Use the database for stateful checks (e.g., velocity).

**Why:**
- Supports running multiple replicas behind a load balancer.
- Avoids in-memory counters that break on restart or multi-instance deployments.

## 10) Logging: include transactionId for traceability

**Decision:** Include `transactionId` in log context (MDC) so logs across service layers and integrations can be correlated.

**Why:** Real-world debugging depends on correlating logs for a single transaction across service layers and integrations.
