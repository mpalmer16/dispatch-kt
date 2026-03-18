package dkt.dispatch.service

import dkt.dispatch.events.OrderCreatedEvent
import dkt.dispatch.persistence.DispatchJob
import dkt.dispatch.persistence.DispatchJobRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.*

@Service
class DispatchPlanningService(
    private val dispatchJobRepository: DispatchJobRepository,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun planDispatch(event: OrderCreatedEvent) {
        val eventId = UUID.fromString(event.eventId)
        val orderId = UUID.fromString(event.payload.orderId)

        if (dispatchJobRepository.existsBySourceEventId(eventId)) {
            logger.info("Event already processed, skipping.")
            return
        }

        val priority = if (event.payload.totalCents >= 10_000) "HIGH" else "NORMAL"
        val region = deriveRegion(event.payload.customerId)

        val job = DispatchJob(
            orderId = orderId,
            customerId = event.payload.customerId,
            status = "pending_assignment",
            region = region,
            priority = priority,
            sourceEventId = UUID.fromString(event.eventId),
            createdAt = OffsetDateTime.now(),
        )
        dispatchJobRepository.save(job)

        logger.info(
            "Planned Dispatch for oderId={}, region={}, priority={}",
            event.payload.orderId,
            region,
            priority
        )
    }

    private fun deriveRegion(customerId: String): String =
        when (customerId.firstOrNull()?.uppercaseChar()) {
            in 'A'..'G' -> "NORTH"
            in 'H'..'N' -> "CENTRAL"
            else -> "SOUTH"
        }
}