package dkt.dispatch.consumer

import dkt.dispatch.events.OrderCreatedEvent
import dkt.dispatch.service.DispatchPlanningService
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class OrderCreatedConsumer(
    private val dispatchPlanningService: DispatchPlanningService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @KafkaListener(
        topics = ["orders.created"],
        groupId = "dispatch-kt",
    )
    fun consume(event: OrderCreatedEvent) {
        logger.info("Received order.created event: {}", event)
        dispatchPlanningService.planDispatch(event)
    }

}