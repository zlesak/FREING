package customer_service.repo

import customer_service.models.CustomerEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CustomerRepo: JpaRepository<CustomerEntity, Long> {
    fun findByEmailOrPhoneNumber(email: String, phoneNumber: String): CustomerEntity?
}