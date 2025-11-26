package customer_service.service

import com.uhk.fim.prototype.common.exceptions.NotFoundException
import com.uhk.fim.prototype.common.exceptions.WrongDataException
import customer_service.external.AresClient
import customer_service.external.KeycloakAdminClient
import customer_service.models.Customer
import customer_service.repo.CustomerRepo
import org.keycloak.representations.idm.UserRepresentation
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CustomerService(
    private val customerRepo: CustomerRepo,
    private val aresClient: AresClient,
    private val keycloakAdminClient: KeycloakAdminClient
) {

    @Transactional
    fun create(customer: Customer): Customer {
        when {
            getCustomerByEmailOrPhoneNumber(customer.email, customer.phoneNumber) != null ->
                throw WrongDataException("Customer already exists!")

            !(customer.tradeName.isNotBlank() xor (customer.name.isNotBlank() && customer.surname.isNotBlank())) ->
                throw WrongDataException("Fill in either the first name and last name, or the trade name, not both!")

            customer.phoneNumber.isBlank() ->
                throw WrongDataException("Customer phone must be fill!")

            customer.email.isBlank() ->
                throw WrongDataException("Customer email must be fill!")
        }

        val savedCustomer = customerRepo.save(customer)

        val user = UserRepresentation().apply {
            username = savedCustomer.email
            email = savedCustomer.email
            firstName = savedCustomer.tradeName.ifBlank { savedCustomer.name }
            lastName = if (savedCustomer.tradeName.isNotBlank()) "" else savedCustomer.surname
            isEnabled = true
            attributes = mapOf("db_id" to listOf(savedCustomer.id.toString()))
        }

        val userId: String = try {
            keycloakAdminClient.createUser(user)
        } catch (ex: Exception) {
            customerRepo.deleteById(savedCustomer.id!!)
            throw RuntimeException("Failed to create user in Keycloak: ${ex.message}", ex)
        }

        try {
            keycloakAdminClient.addRealmRoleToUser(userId, "customer")
            keycloakAdminClient.sendUpdatePasswordEmail(userId)
        } catch (ex: Exception) {
            try {
                keycloakAdminClient.deleteUser(userId)
            } catch (_: Exception) {
            }
            customerRepo.deleteById(savedCustomer.id!!)
            throw RuntimeException("Failed to assign role or send email to Keycloak user: ${ex.message}", ex)
        }

        return savedCustomer
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
            ?: throw NotFoundException("Customer not found!")

    private fun getCustomerByEmailOrPhoneNumber(email: String, phoneNumber: String): Customer? =
        customerRepo.findByEmailOrPhoneNumber(email, phoneNumber)?.takeIf { !it.deleted }

    fun getAllCustomers(pageable: Pageable): Page<Customer> = customerRepo.findAll(pageable)

    fun getCustomersNotDeleted(pageable: Pageable): Page<Customer> =
        customerRepo.findAllByDeletedFalse(pageable)

    fun getCustomerFromAres(ico: String): Customer =
        aresClient.getSubjectByIcoARES(ico)?.toCustomerEntity()
            ?: throw WrongDataException("Prázdné tělo odpovědi ARES pro ICO $ico")
}
