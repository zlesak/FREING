package customer_service.controller

import customer_service.dto.customer.request.CreateCustomerDto
import customer_service.dto.customer.response.CustomerDto
import customer_service.dto.customer.response.CustomersPagedResponse
import customer_service.models.CustomerEntity
import customer_service.service.CustomerService
import io.swagger.v3.oas.annotations.Parameter
import org.springframework.data.domain.PageRequest
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/customers")
class CustomerController(
    private val customerService: CustomerService
) {

    @PostMapping("/create")
    fun create(@RequestBody customer: CreateCustomerDto): CustomerDto {
        return customerService.create(customer.toEntity()).toDto()
    }

    @PostMapping("/update")
    fun update(@RequestBody customer: CustomerDto): CustomerDto {
        return customerService.update(customer.toEntity()).toDto()
    }

    @DeleteMapping("delete/{id}")
    fun delete(@PathVariable id: Long) {
        customerService.deleteCustomer(id)
    }

    @GetMapping("/get-by-id/{id}")
    fun getById(@PathVariable("id") id: Long): CustomerEntity {
        return customerService.getCustomerById(id)
    }

    @GetMapping("/get-customers-paged")
    fun getAll(
        @Parameter(description = "Číslo stránky", example = "0")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Velikost stránky", example = "10")
        @RequestParam(defaultValue = "10") size: Int
    ): CustomersPagedResponse<CustomerEntity> {
        val pageable = PageRequest.of(page, size)
        return customerService.getAllCustomers(pageable)
    }

    @GetMapping("/get-customers-not-deleted-paged")
    fun getAllNotDeleted(
        @Parameter(description = "Číslo stránky", example = "0")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Velikost stránky", example = "10")
        @RequestParam(defaultValue = "10") size: Int
    ): CustomersPagedResponse<CustomerEntity> {
        val pageable = PageRequest.of(page, size)
        return customerService.getCustomersNotDeleted(pageable)
    }

    @GetMapping("/get-customers-info-from-ares/{ico}")

    fun getCustomerInfoFromAresByIco(@PathVariable ico: String): CustomerEntity {
        return customerService.getCustomerFromAres(ico)
    }
}
