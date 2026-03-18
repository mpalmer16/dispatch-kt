package dkt.dispatch.events

import com.fasterxml.jackson.module.kotlin.KotlinInvalidNullException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dkt.dispatch.support.orderCreatedEventJson
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class OrderCreatedEventJsonTest {

    private val objectMapper = jacksonObjectMapper()

    @Test
    fun `deserializes a kafka order created payload into the event model`() {
        val event = objectMapper.readValue<OrderCreatedEvent>(orderCreatedEventJson())
        assertEquals("2026-01-15T10:15:30Z", event.occurredAt)
    }

    @Test
    fun `rejects payloads that are missing required fields`() {
        assertThrows<KotlinInvalidNullException> { objectMapper.readValue<OrderCreatedEvent>("""{"eventId":"missing-payload"}""") }
    }
}
