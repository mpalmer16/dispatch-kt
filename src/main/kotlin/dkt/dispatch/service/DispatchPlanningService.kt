package dkt.dispatch.service

import dkt.dispatch.events.OrderCreatedEvent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class DispatchPlanningService {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun planDispatch(event: OrderCreatedEvent) {
        val priority = if (event.payload.totalCents >= 10_000) "HIGH" else "MEDIUM"
        val region = deriveRegion(event.payload.customerId)

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