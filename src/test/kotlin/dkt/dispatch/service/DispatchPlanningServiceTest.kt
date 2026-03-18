package dkt.dispatch.service

import dkt.dispatch.events.OrderCreatedEvent
import dkt.dispatch.persistence.DispatchJob
import dkt.dispatch.persistence.DispatchJobRepository
import dkt.dispatch.support.orderCreatedEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import java.time.OffsetDateTime
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

        @Test
        fun `creates a pending HIGH priority job for large northern orders`() {
            val event = orderCreatedEvent(customerId = "A-100", totalCents = 10_000)
            val before = OffsetDateTime.now()

            val savedJob = planDispatchForNewEvent(event)

            val after = OffsetDateTime.now()

            assertThat(savedJob.id).isNull()
            assertThat(savedJob.orderId).isEqualTo(UUID.fromString(event.payload.orderId))
            assertThat(savedJob.customerId).isEqualTo(event.payload.customerId)
            assertThat(savedJob.status).isEqualTo("pending_assignment")
            assertThat(savedJob.region).isEqualTo("NORTH")
            assertThat(savedJob.sourceEventId).isEqualTo(UUID.fromString(event.eventId))
            assertThat(savedJob.createdAt).isBetween(before, after)
        }

        @Test
        fun `creates a NORMAL priority job for smaller central orders`() {
            val event = orderCreatedEvent(customerId = "H-200", totalCents = 9_999)

            val savedJob = planDispatchForNewEvent(event)

            assertThat(savedJob.status).isEqualTo("pending_assignment")
            assertThat(savedJob.priority).isEqualTo("NORMAL")
            assertThat(savedJob.region).isEqualTo("CENTRAL")
            assertThat(savedJob.sourceEventId).isEqualTo(UUID.fromString(event.eventId))
        }

        @Test
        fun `routes customers outside the north and central ranges to SOUTH`() {
            val event = orderCreatedEvent(customerId = "Z-300")

            val savedJob = planDispatchForNewEvent(event)

            assertThat(savedJob.region).isEqualTo("SOUTH")
            assertThat(savedJob.priority).isEqualTo("HIGH")
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

        @Test
        fun `fails fast when the event id is not a UUID`() {
            val event = orderCreatedEvent(eventId = "not-a-uuid")

            assertThrows<IllegalArgumentException> { subject.planDispatch(event) }

            verify(dispatchJobRepository, never()).save(any(DispatchJob::class.java))
        }

        @Test
        fun `fails fast when the payload order id is not a UUID`() {
            val event = orderCreatedEvent(orderId = "not-a-uuid")

            assertThrows<IllegalArgumentException> { subject.planDispatch(event) }
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
