package dkt.dispatch.service

import dkt.dispatch.events.OrderCreatedEvent
import dkt.dispatch.persistence.DispatchJob
import dkt.dispatch.persistence.DispatchJobRepository
import dkt.dispatch.support.orderCreatedEvent
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import java.util.*

class DispatchPlanningServiceTest {

    private lateinit var dispatchJobRepository: DispatchJobRepository
    private lateinit var subject: DispatchPlanningService

    @BeforeEach
    fun setUp() {
        dispatchJobRepository = mock(DispatchJobRepository::class.java)
        subject = DispatchPlanningService(dispatchJobRepository)
    }

    @Nested
    inner class NewEvents {

        @Disabled("Test shell: add assertions for the saved dispatch job")
        @Test
        fun `creates a pending HIGH priority job for large northern orders`() {
            val event = orderCreatedEvent(customerId = "A-100", totalCents = 10_000)

            planDispatchForNewEvent(event)
        }

        @Disabled("Test shell: add assertions for the saved dispatch job")
        @Test
        fun `creates a NORMAL priority job for smaller central orders`() {
            val event = orderCreatedEvent(customerId = "H-200", totalCents = 9_999)

            planDispatchForNewEvent(event)
        }

        @Disabled("Test shell: add assertions for regional mapping")
        @Test
        fun `routes customers outside the north and central ranges to SOUTH`() {
            val event = orderCreatedEvent(customerId = "Z-300")

            planDispatchForNewEvent(event)
        }
    }

    @Nested
    inner class DuplicateEvents {

        @Test
        fun `does not save a second dispatch job for an already processed event`() {
            val event = orderCreatedEvent()
            givenEventAlreadyProcessed(event)

            subject.planDispatch(event)

            verify(dispatchJobRepository).existsBySourceEventId(UUID.fromString(event.eventId))
            verify(dispatchJobRepository, never()).save(any(DispatchJob::class.java))
            verifyNoMoreInteractions(dispatchJobRepository)
        }
    }

    @Nested
    inner class InvalidIdentifiers {

        @Disabled("Test shell: add failure assertions for malformed ids")
        @Test
        fun `fails fast when the event id is not a UUID`() {
            val event = orderCreatedEvent(eventId = "not-a-uuid")

            subject.planDispatch(event)
        }

        @Disabled("Test shell: add failure assertions for malformed ids")
        @Test
        fun `fails fast when the payload order id is not a UUID`() {
            val event = orderCreatedEvent(orderId = "not-a-uuid")

            subject.planDispatch(event)
        }
    }

    private fun planDispatchForNewEvent(event: OrderCreatedEvent): DispatchJob {
        givenEventHasNotBeenProcessed(event)

        subject.planDispatch(event)

        return captureSavedJob()
    }

    private fun givenEventHasNotBeenProcessed(event: OrderCreatedEvent) {
        `when`(dispatchJobRepository.existsBySourceEventId(UUID.fromString(event.eventId)))
            .thenReturn(false)
        doAnswer { invocation -> invocation.getArgument<DispatchJob>(0) }
            .`when`(dispatchJobRepository)
            .save(any(DispatchJob::class.java))
    }

    private fun givenEventAlreadyProcessed(event: OrderCreatedEvent) {
        `when`(dispatchJobRepository.existsBySourceEventId(UUID.fromString(event.eventId)))
            .thenReturn(true)
    }

    private fun captureSavedJob(): DispatchJob {
        val captor = ArgumentCaptor.forClass(DispatchJob::class.java)
        verify(dispatchJobRepository).save(captor.capture())
        return captor.value
    }
}
