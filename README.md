# dispatch-kt

`dispatch-kt` is the Kotlin dispatch planning service. It consumes `order.created` events from Kafka and writes a `dispatch_jobs` row in Postgres for downstream dispatch work.

## What It Does

When an `order.created` event arrives, this service:

1. deserializes the event payload
2. checks whether the source event has already been processed
3. derives a dispatch region from the customer ID
4. assigns a priority from the order total
5. writes a row to `dispatch_jobs`

The consumer treats `source_event_id` as the deduplication key so the same event is not planned twice.

## Event Contract

This service listens to the Kafka topic:

```text
orders.created
```

It expects an event shaped like:

```json
{
  "eventId": "uuid",
  "eventType": "order.created",
  "occurredAt": "2026-03-21T12:00:00Z",
  "payload": {
    "orderId": "uuid",
    "customerId": "customer-123",
    "totalCents": 4999,
    "status": "created"
  }
}
```

## Configuration

The main runtime settings come from environment variables:

- `KAFKA_BOOTSTRAP_SERVERS`
  Default: `localhost:9092`
- `KAFKA_CONSUMER_GROUP_ID`
  Default: `dispatch-kt`
- `SPRING_DATASOURCE_URL`
  Default: `jdbc:postgresql://localhost:5432/dispatch`
- `SPRING_DATASOURCE_USERNAME`
  Default: `dispatch`
- `SPRING_DATASOURCE_PASSWORD`
  Default: `dispatch`
- `APP_LOG_LEVEL`
  Default: `DEBUG`

## Local Run

From the parent workspace, start shared infrastructure first:

```bash
cd ..
make local-setup
```

Then run the service from this repo:

```bash
./gradlew bootRun
```

This service is typically run alongside:

- `dispatch`, which writes the order and outbox row
- `dispatch-go`, which relays the outbox event to Kafka

## Tests

Run the Kotlin test suite with:

```bash
./gradlew test
```

## How It Fits

`dispatch-kt` is the downstream consumer in the current system:

1. Rust accepts the order and writes the outbox event.
2. Go publishes the event to Kafka.
3. Kotlin consumes the event and persists the dispatch planning result.
