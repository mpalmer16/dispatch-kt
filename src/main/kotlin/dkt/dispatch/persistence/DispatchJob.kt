package dkt.dispatch.persistence

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.OffsetDateTime
import java.util.*

@Table("dispatch_jobs")
data class DispatchJob(
    @Id
    val id: UUID? = null,

    @Column("order_id")
    val orderId: UUID,

    @Column("customer_id")
    val customerId: String,

    val status: String,
    val region: String,
    val priority: String,

    @Column("source_event_id")
    val sourceEventId: UUID,

    @Column("created_at")
    val createdAt: OffsetDateTime
)