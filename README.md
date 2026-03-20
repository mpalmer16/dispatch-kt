# dispatch-kt

`dispatch-kt` is the Kotlin service in the dispatch practice project. It is part of a larger, locally run system
designed to model a realistic event-driven backend architecture using Kafka, Postgres, and multiple services.

This project focuses on building and understanding:

- event-driven workflows
- Kafka-based messaging
- service boundaries and responsibilities
- local development using Docker Compose
- testing strategies for distributed systems

---

## Overview

At a high level, `dispatch-kt` is responsible for:

- connecting to Kafka
- consuming dispatch-related messages
- processing messages through application logic
- optionally persisting or forwarding results
- producing follow-up messages when appropriate

> This service is intentionally designed as a learning and experimentation environment rather than a minimal example.

---

## Project Structure

This repo is intended to run alongside other components in a shared parent directory:

```text
parent-directory/
├── dispatch/        # Rust service (DB, migrations, core backend)
├── dispatch-kt/     # Kotlin service (this repo)
└── docker-compose.yml
````

---

## Prerequisites

Make sure you have the following installed:

* Java 21+
* Gradle (or use the included `./gradlew`)
* Docker
* Docker Compose

---

## Local Development Setup

### 1. Start Infrastructure

From the parent directory:

```bash
docker compose up -d
```

This should start:

* Kafka
* Zookeeper (if applicable)
* Postgres

---

### 2. Run Database Migrations

From the `dispatch` repo:

```bash
cd dispatch
sqlx migrate run
cd ..
```

---

### 3. Create Kafka Topic

Example (adjust container name as needed):

```bash
docker exec -it <kafka-container> kafka-topics \
  --bootstrap-server localhost:9092 \
  --create \
  --if-not-exists \
  --topic orders.created
```

---

### 4. Start dispatch-kt

From this repo:

```bash
./gradlew run
```

Or if using Spring Boot:

```bash
./gradlew bootRun
```

Keep `dispatch-kt` running before using the parent repo's `./verify-flow.sh` script.

---

## Configuration

Configuration is expected to come from environment variables and/or application config files.

Typical values include:

| Variable                  | Description                |
|---------------------------|----------------------------|
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka connection string    |
| `DISPATCH_TOPIC`          | Kafka topic name           |
| `DATABASE_URL`            | Postgres connection string |
| `DATABASE_USER`           | DB username                |
| `DATABASE_PASSWORD`       | DB password                |

---

## Testing

Run tests with:

```bash
./gradlew test
```

---

## Testing Strategy

This project aims to include multiple layers of testing:

### Unit Tests

Fast, isolated tests for:

* message parsing
* validation logic
* business rules
* transformation/mapping logic

### Integration Tests

Tests that verify interaction with real dependencies:

* Kafka (via Testcontainers)
* database (optional)

### Behavioral Tests

Focused tests around core workflows:

* valid message → expected processing
* invalid message → safe failure
* duplicate/idempotent message handling
* downstream message production

---

## Example Test Areas

* message deserialization works correctly
* invalid payloads are handled safely
* handler performs expected actions
* duplicate messages do not cause duplicate effects
* outbound events are produced correctly
* failure paths do not crash the consumer

---

## Purpose

This project is intentionally designed to simulate real-world backend development scenarios, including:

* asynchronous workflows
* distributed systems concerns
* data consistency challenges
* service-to-service communication

It is not a toy example — the goal is to practice building something that feels like production software.
