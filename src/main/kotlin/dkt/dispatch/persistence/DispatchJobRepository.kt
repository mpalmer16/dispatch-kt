package dkt.dispatch.persistence

import org.springframework.data.repository.CrudRepository
import java.util.*

interface DispatchJobRepository : CrudRepository<DispatchJob, UUID> {
    fun existsByOrderId(orderId: UUID): Boolean
}