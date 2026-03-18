package dkt.dispatch.consumer

import dkt.dispatch.service.DispatchPlanningService
import dkt.dispatch.support.orderCreatedEvent
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito

class OrderCreatedConsumerTest {

    private lateinit var dispatchPlanningService: DispatchPlanningService
    private lateinit var subject: OrderCreatedConsumer

    @BeforeEach
    fun setUp() {
        dispatchPlanningService = Mockito.mock(DispatchPlanningService::class.java)
        subject = OrderCreatedConsumer(dispatchPlanningService)
    }

    @Test
    fun `forwards each consumed event to dispatch planning`() {
        val event = orderCreatedEvent()

        subject.consume(event)

        Mockito.verify(dispatchPlanningService).planDispatch(event)
    }
}
