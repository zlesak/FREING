package customer_service.service

import customer_service.external.AresClient
import com.uhk.fim.prototype.common.exceptions.NotFoundException
import com.uhk.fim.prototype.common.exceptions.WrongDataException
import customer_service.dto.customer.response.CustomersPagedResponse
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
        getCustomerByEmailOrPhoneNumber(customer.email, customer.phoneNumber)?.let { throw WrongDataException("Customer already exists!") }

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
        val existedCustomer = getCustomerByEmailOrPhoneNumber(customer.email, customer.phoneNumber) ?: throw NotFoundException("Customer not found!")


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
        customerRepo.deleteById(id)
    }

    fun getCustomerById(id: Long): CustomerEntity {
        return customerRepo.findByIdOrNull(id) ?: throw NotFoundException("Customer not found!")
    }


    private fun getCustomerByEmailOrPhoneNumber(email: String, phoneNumber: String): CustomerEntity? {
       return customerRepo.findByEmailOrPhoneNumber(email, phoneNumber)
    }

    fun getAllCustomers(pageable: Pageable): CustomersPagedResponse<CustomerEntity> {
        val allCustomers = customerRepo.findAll()

        val startIndex = pageable.pageNumber * pageable.pageSize
        val endIndex = minOf(startIndex + pageable.pageSize, allCustomers.size)
        val pageContent = if (startIndex < allCustomers.size) {
            allCustomers.subList(startIndex, endIndex)
        } else {
            emptyList<CustomerEntity>()
        }

        return CustomersPagedResponse(
            content = pageContent,
            totalElements = allCustomers.size.toLong(),
            totalPages = (allCustomers.size + pageable.pageSize - 1) / pageable.pageSize,
            page = pageable.pageNumber,
            size = pageable.pageSize
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