package customer_service.service

import com.uhk.fim.prototype.common.exceptions.NotFoundException
import com.uhk.fim.prototype.common.exceptions.WrongDataException
import customer_service.models.CustomerEntity
import customer_service.repo.CustomerRepo
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class CustomerService(
    private val customerRepo: CustomerRepo
) {

    fun create(customer: CustomerEntity): CustomerEntity {
        getCustomerByEmailOrPhoneNumber(customer.email, customer.phoneNumber)?.let { throw WrongDataException("Customer already exists!") }

        if (customer.name.isBlank()) throw WrongDataException("Customer name must be fill!")
        if (customer.surname.isBlank()) throw WrongDataException("Customer surname must be fill!")
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
}