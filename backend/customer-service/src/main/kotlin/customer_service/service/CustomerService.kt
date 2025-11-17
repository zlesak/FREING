package customer_service.service

import com.uhk.fim.prototype.common.exceptions.NotFoundException
import com.uhk.fim.prototype.common.exceptions.WrongDataException
import com.uhk.fim.prototype.common.exceptions.customer.CustomerNotFoundException
import customer_service.external.AresClient
import customer_service.models.Customer
import customer_service.repo.CustomerRepo
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class CustomerService(
    private val customerRepo: CustomerRepo,
    private val aresClient: AresClient
) {

    fun create(customer: Customer): Customer = when {
        getCustomerByEmailOrPhoneNumber(customer.email, customer.phoneNumber) != null ->
            throw WrongDataException("Customer already exists!")

        !(customer.tradeName.isNotBlank() xor (customer.name.isNotBlank() && customer.surname.isNotBlank())) ->
            throw WrongDataException("Fill in either the first name and last name, or the trade name, not both!")

        customer.phoneNumber.isBlank() ->
            throw WrongDataException("Customer phone must be fill!")

        customer.email.isBlank() ->
            throw WrongDataException("Customer email must be fill!")

        else -> customerRepo.save(customer)
    }

    fun update(customer: Customer): Customer =
        getCustomerById(customer.id!!, false).apply { updateFrom(customer) }
            .let { customerRepo.save(it) }

    fun deleteCustomer(id: Long) =
        customerRepo.findByIdOrNull(id)?.let {
            it.deleted = true
            customerRepo.save(it)
        } ?: throw NotFoundException("Customer not found!")

    fun getCustomerById(id: Long, fromMessaging: Boolean = false): Customer =
        customerRepo.findByIdOrNull(id)
            ?.takeUnless { !fromMessaging && it.deleted }
            ?: throw CustomerNotFoundException("Customer not found!")

    private fun getCustomerByEmailOrPhoneNumber(email: String, phoneNumber: String): Customer? =
        customerRepo.findByEmailOrPhoneNumber(email, phoneNumber)?.takeIf { !it.deleted }

    fun getAllCustomers(pageable: Pageable): Page<Customer> = customerRepo.findAll(pageable)

    fun getCustomersNotDeleted(pageable: Pageable): Page<Customer> = customerRepo.findAllByDeletedFalse(pageable)

    fun getCustomerFromAres(ico: String): Customer = aresClient.getSubjectByIcoARES(ico)?.toCustomerEntity()
        ?: throw WrongDataException("Prázdné tělo odpovědi ARES pro ICO $ico")
}