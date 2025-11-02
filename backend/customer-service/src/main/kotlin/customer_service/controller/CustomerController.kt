package customer_service.controller

import customer_service.dto.request.CreateCustomerDto
import customer_service.dto.response.CustomerDto
import customer_service.dto.response.toDto
import customer_service.dto.response.toEntity
import customer_service.models.CustomerEntity
import customer_service.service.CustomerService
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
}