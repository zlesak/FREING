package customer_service.repo

import customer_service.models.Customer
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CustomerRepo: JpaRepository<Customer, Long> {
    fun findByEmailOrPhoneNumber(email: String, phoneNumber: String): Customer?

    fun findAllByDeletedFalse(pageable: Pageable): Page<Customer>
}