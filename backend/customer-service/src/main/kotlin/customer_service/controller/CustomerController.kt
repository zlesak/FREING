package customer_service.controller

import customer_service.dto.customer.request.CreateCustomerDto
import customer_service.dto.customer.response.CustomerDto
import customer_service.models.Customer
import customer_service.service.CustomerService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.security.access.prepost.PostAuthorize
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/customers")
class CustomerController(
    private val customerService: CustomerService
) {

    @PreAuthorize("hasAnyAuthority('SCOPE_service.call', 'ROLE_MANAGER', 'ROLE_ACCOUNTANT')")
    @Operation(operationId = "createCustomer")
    @PostMapping("/create")
    fun create(@RequestBody customer: CreateCustomerDto): CustomerDto =
        customerService.create(customer.toEntity()).toDto()

    @PreAuthorize("hasAnyAuthority('SCOPE_service.call', 'ROLE_MANAGER', 'ROLE_ACCOUNTANT')")
    @Operation(operationId = "updateCustomer")
    @PostMapping("/update")
    fun update(@RequestBody customer: CustomerDto): CustomerDto = customerService.update(customer.toEntity()).toDto()

    @PreAuthorize("hasAnyAuthority('SCOPE_service.call', 'ROLE_MANAGER', 'ROLE_ACCOUNTANT')")
    @Operation(operationId = "deleteCustomer")
    @DeleteMapping("delete/{id}")
    fun delete(@PathVariable id: Long) = customerService.deleteCustomer(id)

    @Operation(operationId = "getCustomerById")
    @GetMapping("/get-by-id/{id}")
    @PostAuthorize("hasAnyAuthority('SCOPE_service.call', 'ROLE_MANAGER', 'ROLE_ACCOUNTANT') or returnObject.id == authentication.principal.id")
    fun getById(@PathVariable("id") id: Long): Customer = customerService.getCustomerById(id)

    @PreAuthorize("hasAnyAuthority('SCOPE_service.call', 'ROLE_MANAGER', 'ROLE_ACCOUNTANT', 'ROLE_CUSTOMER')")
    @Operation(operationId = "getAllCustomers")
    @GetMapping("/get-customers-paged")
    fun getAll(
        authentication: Authentication,
        @Parameter(description = "Číslo stránky", example = "0")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Velikost stránky", example = "10")
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(required = false) customerId: Long?,
        @RequestParam(required = false) customerIds: List<Long>?
    ): Page<CustomerDto> {
        val isCustomer = authentication.authorities.any { it.authority == "ROLE_CUSTOMER" }
        val principal = authentication.principal as? com.uhk.fim.prototype.common.security.JwtUserPrincipal
        val effectiveCustomerIds = if (isCustomer) listOf(
            principal?.id ?: throw IllegalStateException("Customer principal does not contain a valid id.")
        ) else customerIds
        val effectiveCustomerId = if (isCustomer) principal?.id else customerId
        return customerService.getAllCustomers(PageRequest.of(page, size), effectiveCustomerId, effectiveCustomerIds)
            .map { it.toDto() }
    }

    @PreAuthorize("hasAnyAuthority('SCOPE_service.call', 'ROLE_MANAGER', 'ROLE_ACCOUNTANT')")
    @Operation(operationId = "getAllCustomersNotDeleted")
    @GetMapping("/get-customers-not-deleted-paged")
    fun getAllNotDeleted(
        @Parameter(description = "Číslo stránky", example = "0")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Velikost stránky", example = "10")
        @RequestParam(defaultValue = "10") size: Int
    ): Page<CustomerDto> = customerService.getCustomersNotDeleted(PageRequest.of(page, size)).map { it.toDto() }

    @PreAuthorize("hasAnyAuthority('SCOPE_service.call', 'ROLE_MANAGER', 'ROLE_ACCOUNTANT')")
    @Operation(operationId = "getCustomerInfoFromAres")
    @GetMapping("/get-customers-info-from-ares/{ico}")
    fun getCustomerInfoFromAresByIco(@PathVariable ico: String): Customer =
        customerService.getCustomerFromAres(ico)
}
