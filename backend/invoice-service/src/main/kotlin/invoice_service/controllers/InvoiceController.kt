package invoice_service.controllers

import com.uhk.fim.prototype.common.security.JwtUserPrincipal
import invoice_service.dtos.invoices.requests.InvoiceCreateRequest
import invoice_service.dtos.invoices.requests.InvoiceUpdateRequest
import invoice_service.models.invoices.Invoice
import invoice_service.models.invoices.InvoiceStatusEnum
import invoice_service.services.InvoiceService
import invoice_service.services.ZugferdService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.security.access.prepost.PostAuthorize
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@Tag(name = "Invoices", description = "API pro správu faktur")
@RestController
@RequestMapping("/api/invoices")
class InvoiceController(
    private val service: InvoiceService,
    private val zugferdService: ZugferdService
) {

    @Operation(summary = "Získat fakturu podle ID", description = "Vrací detail faktury podle jejího ID.")
    @PostAuthorize("hasRole('ACCOUNTANT') or hasRole('MANAGER') or (returnObject.customerId == authentication.principal.id and returnObject.status.name() != 'DRAFT')")
    @GetMapping("/get-by-id/{id}")
    fun getInvoice(
        authentication: Authentication,
        @Parameter(description = "ID faktury", example = "1")
        @PathVariable id: Long
    ): Invoice = service.getInvoice(id)

    @Operation(summary = "Získat xml pro fakturu podle ID", description = "Vrací xml faktury podle jejího ID.")

    @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('MANAGER')")
    @GetMapping("/get-by-id/{id}/xml")
    fun getInvoiceXml(
        @Parameter(description = "ID faktury", example = "1")
        @PathVariable id: Long
    ): String {
        return zugferdService.createInvoice(id)
    }

    @Operation(summary = "Vytvořit novou fakturu", description = "Vytvoří novou fakturu na základě požadavku.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Faktura úspěšně vytvořena",
                content = [Content(schema = Schema(implementation = Invoice::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Faktura s tímto číslem již existuje",
                content = [Content(schema = Schema(example = "{\"error\": \"Faktura s tímto číslem již existuje.\"}"))]
            )
        ]
    )
    @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('MANAGER')")
    @PostMapping("/create")
    fun createInvoice(
        @Parameter(description = "Data pro vytvoření faktury")
        @RequestBody request: InvoiceCreateRequest
    ): Invoice = service.createInvoice(request)

    @Operation(summary = "Aktualizovat fakturu", description = "Aktualizuje existující fakturu podle ID.")
    @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('MANAGER')")
    @PutMapping("/update/{id}")
    fun updateInvoice(
        @Parameter(description = "ID faktury", example = "1")
        @PathVariable id: Long,
        @Parameter(description = "Data pro aktualizaci faktury")
        @RequestBody request: InvoiceUpdateRequest
    ): Invoice = service.updateInvoice(id, request)

    @Operation(summary = "Smazat fakturu", description = "Smaže fakturu podle ID.")
    @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('MANAGER')")
    @DeleteMapping("/delete/{id}")
    fun deleteInvoice(
        @Parameter(description = "ID faktury", example = "1")
        @PathVariable id: Long
    ) = service.deleteInvoice(id)

    @Operation(summary = "Faktura přečtena", description = "Posune fakturu do stav upending jelikož ji zákazník přečetl.")
    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/read/{id}")
    fun markInvoiceAsRead(
        authentication: Authentication,
        @Parameter(description = "ID faktury", example = "1")
        @PathVariable id: Long
    ) {
        getInvoice(authentication, id)
        service.markInvoiceAsRead(id)
    }

    @Operation(summary = "Získat faktury", description = "Vrací stránkovaný seznam faktur. Pokud nejsou nastaveny žádné filtry, vrací všechny faktury.")
    @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('MANAGER') or hasRole('CUSTOMER')")
    @GetMapping("/get-invoices")
    fun getInvoices(
        authentication: Authentication,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(required = false) dateFrom: String?,
        @RequestParam(required = false) dateTo: String?,
        @RequestParam(required = false) customerId: Long?,
        @RequestParam(required = false) status: InvoiceStatusEnum?,
        @RequestParam(required = false) amountFrom: Double?,
        @RequestParam(required = false) amountTo: Double?,
        @RequestParam(required = false) currency: String?
    ): Page<Invoice> {
        val principal = authentication.principal as? JwtUserPrincipal ?: throw IllegalStateException("Invalid principal type")
        val isCustomer = authentication.authorities.any { it.authority == "ROLE_CUSTOMER" }
        val effectiveCustomerId = if (isCustomer) principal.id else customerId
        val dateFromParsed = dateFrom?.let { java.time.LocalDate.parse(it) }
        val dateToParsed = dateTo?.let { java.time.LocalDate.parse(it) }
        return service.getFilteredInvoices(
            PageRequest.of(page, size),
            dateFromParsed,
            dateToParsed,
            effectiveCustomerId,
            status,
            amountFrom,
            amountTo,
            currency,
            isCustomer
        )
    }
}