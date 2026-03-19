package dkt.dispatch

import dkt.dispatch.persistence.DispatchJob
import dkt.dispatch.persistence.DispatchJobRepository
import dkt.dispatch.support.orderCreatedEventJson
import org.apache.kafka.common.serialization.StringSerializer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.kafka.test.utils.KafkaTestUtils
import org.springframework.test.annotation.DirtiesContext
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.Duration
import java.util.*
import kotlin.test.Test

@Testcontainers
@SpringBootTest(properties = ["spring.sql.init.mode=always"])
@DirtiesContext
@EmbeddedKafka(
    topics = ["orders.created"],
    bootstrapServersProperty = "spring.kafka.bootstrap-servers",
)
class DispatchKtApplicationTests {

    @org.springframework.beans.factory.annotation.Autowired
    private lateinit var dispatchJobRepository: DispatchJobRepository

    @org.springframework.beans.factory.annotation.Autowired
    private lateinit var embeddedKafkaBroker: EmbeddedKafkaBroker

    private lateinit var producerFactory: DefaultKafkaProducerFactory<String, String>
    private lateinit var kafkaTemplate: KafkaTemplate<String, String>

    @BeforeEach
    fun setUp() {
        dispatchJobRepository.deleteAll()
        producerFactory = DefaultKafkaProducerFactory(
            KafkaTestUtils.producerProps(embeddedKafkaBroker),
            StringSerializer(),
            StringSerializer(),
        )
        kafkaTemplate = KafkaTemplate(
            producerFactory
        )
    }

    @AfterEach
    fun tearDown() {
        producerFactory.destroy()
    }

    @Test
    fun `starts the application against test infrastructure`() {
        val eventId = "11111111-1111-1111-1111-111111111111"
        val orderId = "22222222-2222-2222-2222-222222222222"

        kafkaTemplate.send(
            "orders.created",
            orderCreatedEventJson(
                eventId = eventId,
                orderId = orderId,
                customerId = "B-100",
                totalCents = 12_500,
            )
        ).get()

        val savedJob = awaitSingleDispatchJob()

        assertThat(savedJob.orderId).isEqualTo(UUID.fromString(orderId))
        assertThat(savedJob.customerId).isEqualTo("B-100")
        assertThat(savedJob.status).isEqualTo("pending_assignment")
        assertThat(savedJob.priority).isEqualTo("HIGH")
        assertThat(savedJob.region).isEqualTo("NORTH")
        assertThat(savedJob.sourceEventId).isEqualTo(UUID.fromString(eventId))
    }

    private fun awaitSingleDispatchJob(timeout: Duration = Duration.ofSeconds(10)): DispatchJob {
        val deadline = System.nanoTime() + timeout.toNanos()
        var jobs = dispatchJobRepository.findAll().toList()

        while (jobs.size != 1 && System.nanoTime() < deadline) {
            Thread.sleep(100)
            jobs = dispatchJobRepository.findAll().toList()
        }

        assertThat(jobs).hasSize(1)
        return jobs.single()
    }

    companion object {
        @Container
        @ServiceConnection
        @JvmStatic
        val postgres = PostgreSQLContainer<Nothing>("postgres:17-alpine")
    }
}
