package customer_service.controller

import customer_service.dto.supplier.request.CreateSupplierDto
import customer_service.dto.supplier.response.SupplierDto
import customer_service.models.Supplier
import customer_service.service.SupplierService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/suppliers")
class SupplierController(
    private val supplierService: SupplierService
) {

    @Operation(operationId = "createSupplier")
    @PreAuthorize("hasAnyAuthority('SCOPE_service.call', 'ROLE_MANAGER', 'ROLE_ACCOUNTANT')")
    @PostMapping("/create")
    fun create(@RequestBody supplier: CreateSupplierDto): SupplierDto =
        supplierService.create(supplier.toEntity()).toDto()

    @Operation(operationId = "updateSupplier")
    @PreAuthorize("hasAnyAuthority('SCOPE_service.call', 'ROLE_MANAGER', 'ROLE_ACCOUNTANT')")
    @PostMapping("/update")
    fun update(@RequestBody supplier: SupplierDto): SupplierDto = supplierService.update(supplier.toEntity()).toDto()

    @Operation(operationId = "deleteSupplier")
    @PreAuthorize("hasAnyAuthority('SCOPE_service.call', 'ROLE_MANAGER', 'ROLE_ACCOUNTANT')")
    @DeleteMapping("delete/{id}")
    fun delete(@PathVariable id: Long): Unit = supplierService.deleteSupplier(id)

    @Operation(operationId = "getSupplierById")
    @PreAuthorize("hasAnyAuthority('SCOPE_service.call', 'ROLE_MANAGER', 'ROLE_ACCOUNTANT', 'ROLE_CUSTOMER')")
    @GetMapping("/get-by-id/{id}")
    fun getById(@PathVariable("id") id: Long): Supplier = supplierService.getSupplierById(id)

    @Operation(operationId = "getAllSuppliers")
    @PreAuthorize("hasAnyAuthority('SCOPE_service.call', 'ROLE_MANAGER', 'ROLE_ACCOUNTANT')")
    @GetMapping("/get-suppliers-paged")
    fun getAll(
        @Parameter(description = "Číslo stránky", example = "0")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Velikost stránky", example = "10")
        @RequestParam(defaultValue = "10") size: Int
    ): Page<SupplierDto> = supplierService.getAllSuppliers(PageRequest.of(page, size)).map { it.toDto() }
}
