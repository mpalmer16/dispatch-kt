package dkt.dispatch.events

data class OrderCreatedEvent(
    val eventId: String,
    val eventType: String,
    val occurredAt: String,
    val payload: OrderCreatedPayload
)

data class OrderCreatedPayload(
    val orderId: String,
    val customerId: String,
    val totalCents: Long,
    val status: String
)