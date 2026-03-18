package dkt.dispatch.support

import dkt.dispatch.events.OrderCreatedEvent
import dkt.dispatch.events.OrderCreatedPayload
import java.time.OffsetDateTime
import java.util.UUID

fun orderCreatedEvent(
    eventId: String = UUID.randomUUID().toString(),
    orderId: String = UUID.randomUUID().toString(),
    customerId: String = "A-100",
    totalCents: Long = 12_500,
    status: String = "created",
    occurredAt: String = OffsetDateTime.parse("2026-01-15T10:15:30Z").toString(),
    eventType: String = "orders.created",
): OrderCreatedEvent =
    OrderCreatedEvent(
        eventId = eventId,
        eventType = eventType,
        occurredAt = occurredAt,
        payload = OrderCreatedPayload(
            orderId = orderId,
            customerId = customerId,
            totalCents = totalCents,
            status = status,
        ),
    )

fun orderCreatedEventJson(
    eventId: String = "11111111-1111-1111-1111-111111111111",
    orderId: String = "22222222-2222-2222-2222-222222222222",
    customerId: String = "A-100",
    totalCents: Long = 12_500,
    status: String = "created",
    occurredAt: String = "2026-01-15T10:15:30Z",
    eventType: String = "orders.created",
): String =
    """
    {
      "eventId": "$eventId",
      "eventType": "$eventType",
      "occurredAt": "$occurredAt",
      "payload": {
        "orderId": "$orderId",
        "customerId": "$customerId",
        "totalCents": $totalCents,
        "status": "$status"
      }
    }
    """.trimIndent()
