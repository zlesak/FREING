package invoice_service.controllers

import invoice_service.dtos.invoices.requests.InvoiceCreateRequest
import invoice_service.dtos.invoices.requests.InvoiceUpdateRequest
import invoice_service.dtos.invoices.responses.InvoicesPagedResponse
import invoice_service.models.invoices.Invoice
import invoice_service.services.InvoiceService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Invoices", description = "API pro správu faktur")
@RestController
@RequestMapping("/api/invoices")
class InvoiceController (private val service: InvoiceService) {

    @Operation(summary = "Získat všechny faktury", description = "Vrací stránkovaný seznam všech faktur.")
    @GetMapping
    fun getAllInvoices(
        @Parameter(description = "Číslo stránky", example = "0")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Velikost stránky", example = "10")
        @RequestParam(defaultValue = "10") size: Int
    ): InvoicesPagedResponse<Invoice> {
        val pageable = PageRequest.of(page, size)
        return service.getAllInvoices(pageable)
    }

    @Operation(summary = "Získat fakturu podle ID", description = "Vrací detail faktury podle jejího ID.")
    @GetMapping("/{id}")
    fun getInvoice(
        @Parameter(description = "ID faktury", example = "1")
        @PathVariable id: Long
    ): ResponseEntity<Invoice> =
        service.getInvoice(id)?.let{ ResponseEntity.ok(it) }?:(ResponseEntity.notFound().build())

    @Operation(summary = "Vytvořit novou fakturu", description = "Vytvoří novou fakturu na základě požadavku.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Faktura úspěšně vytvořena", content = [Content(schema = Schema(implementation = Invoice::class))]),
            ApiResponse(responseCode = "409", description = "Faktura s tímto číslem již existuje", content = [Content(schema = Schema(example = "{\"error\": \"Faktura s tímto číslem již existuje.\"}"))])
        ]
    )
    @PostMapping
    fun createInvoice(
        @Parameter(description = "Data pro vytvoření faktury")
        @RequestBody request: InvoiceCreateRequest
    ):  Invoice {
        return service.createInvoice(request)
    }

    @Operation(summary = "Aktualizovat fakturu", description = "Aktualizuje existující fakturu podle ID.")
    @PutMapping("/{id}")
    fun updateInvoice(
        @Parameter(description = "ID faktury", example = "1")
        @PathVariable id: Long,
        @Parameter(description = "Data pro aktualizaci faktury")
        @RequestBody request: InvoiceUpdateRequest
    ): ResponseEntity<Invoice> {
        val updated = service.updateInvoice(id, request)
        return if (updated != null) ResponseEntity.ok(updated) else ResponseEntity.notFound().build()
    }

    @Operation(summary = "Smazat fakturu", description = "Smaže fakturu podle ID.")
    @DeleteMapping("/{id}")
    fun deleteInvoice(
        @Parameter(description = "ID faktury", example = "1")
        @PathVariable id: Long
    ): ResponseEntity<Void> {
        service.deleteInvoice(id)
        return ResponseEntity.noContent().build()
    }
}