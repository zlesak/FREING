package customer_service.repo

import customer_service.models.Supplier
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository

@Repository
interface SupplierRepo : JpaRepository<Supplier, Long>, JpaSpecificationExecutor<Supplier> {
    fun findByEmailOrPhoneNumber(email: String, phoneNumber: String): Supplier?
}
