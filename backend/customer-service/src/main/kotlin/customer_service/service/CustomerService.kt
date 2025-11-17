package customer_service.service

import com.uhk.fim.prototype.common.exceptions.NotFoundException
import com.uhk.fim.prototype.common.exceptions.WrongDataException
import com.uhk.fim.prototype.common.exceptions.customer.CustomerNotFoundException
import customer_service.dto.customer.response.CustomersPagedResponse
import customer_service.external.AresClient
import customer_service.models.CustomerEntity
import customer_service.repo.CustomerRepo
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class CustomerService(
    private val customerRepo: CustomerRepo,
    private val aresClient: AresClient
) {

    fun create(customer: CustomerEntity): CustomerEntity {
        getCustomerByEmailOrPhoneNumber(
            customer.email,
            customer.phoneNumber
        )?.let { throw WrongDataException("Customer already exists!") }

        if (customer.tradeName.isBlank() && (customer.name.isBlank() || customer.surname.isBlank())) {
            throw WrongDataException("Musíte vyplnit buď jméno a příjmení, nebo obchodní jméno!")
        }
        if (customer.tradeName.isNotBlank() && (customer.name.isNotBlank() || customer.surname.isNotBlank())) {
            throw WrongDataException("Vyplňte buď jméno a příjmení, nebo obchodní jméno, ne obojí!")
        }
        if (customer.phoneNumber.isBlank()) throw WrongDataException("Customer phone must be fill!")
        if (customer.email.isBlank()) throw WrongDataException("Customer email must be fill!")
        return customerRepo.save(customer)
    }

    fun update(customer: CustomerEntity): CustomerEntity {
        val existedCustomer = getCustomerById(customer.id!!, false)

        val updatedCustomer = existedCustomer.apply {
            if (customer.name.isNotBlank()) name = customer.name
            if (customer.surname.isNotBlank()) surname = customer.surname
            if (customer.phoneNumber.isNotBlank()) phoneNumber = customer.phoneNumber
            if (customer.email.isNotBlank()) email = customer.email
            birthDate = customer.birthDate
            street = customer.street
            houseNumber = customer.houseNumber
            city = customer.city
            zip = customer.zip
            country = customer.country
            ico = customer.ico
            dic = customer.dic
            bankCode = customer.bankCode
            bankAccount = customer.bankAccount
            currency = customer.currency
        }

        return customerRepo.save(updatedCustomer)
    }

    fun deleteCustomer(id: Long) {
        val customer = customerRepo.findByIdOrNull(id) ?: throw NotFoundException("Customer not found!")
        customer.deleted = true
        customerRepo.save(customer)
    }

    fun getCustomerById(id: Long, fromMessaging: Boolean = false): CustomerEntity {
        val customer = customerRepo.findByIdOrNull(id) ?: throw CustomerNotFoundException("Customer not found!")
        if (!fromMessaging && customer.deleted) throw CustomerNotFoundException("Customer not found!")
        return customer
    }


    private fun getCustomerByEmailOrPhoneNumber(email: String, phoneNumber: String): CustomerEntity? {
        return customerRepo.findByEmailOrPhoneNumber(email, phoneNumber)?.takeIf { !it.deleted }
    }

    fun getAllCustomers(pageable: Pageable): CustomersPagedResponse<CustomerEntity> {
        val page = customerRepo.findAll(pageable)

        return CustomersPagedResponse(
            content = page.content,
            totalElements = page.totalElements,
            totalPages = page.totalPages,
            page = page.number,
            size = page.size
        )
    }

    fun getCustomersNotDeleted(pageable: Pageable): CustomersPagedResponse<CustomerEntity> {
        val page = customerRepo.findAllByDeletedFalse(pageable)

        return CustomersPagedResponse(
            content = page.content,
            totalElements = page.totalElements,
            totalPages = page.totalPages,
            page = page.number,
            size = page.size
        )
    }

    fun getCustomerFromAres(ico: String): CustomerEntity {
        try {
            val subject = aresClient.getSubjectByIcoARES(ico)
            if (subject != null) {
                return subject.toCustomerEntity()
            } else {
                throw NotFoundException("Nepodařilo se najít subjekt v ARES s IČO: $ico")
            }
        } catch (e: Exception) {
            throw NotFoundException("Chyba při komunikaci s ARES: ${e.message}")
        }
    }
}