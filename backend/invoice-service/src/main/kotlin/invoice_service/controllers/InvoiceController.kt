package invoice_service.controllers

import com.uhk.fim.prototype.common.messaging.dto.InvoiceRequest
import com.uhk.fim.prototype.common.messaging.dto.MessageResponse
import com.uhk.fim.prototype.common.messaging.enums.SourceService
import com.uhk.fim.prototype.common.messaging.enums.invoice.MessageInvoiceAction
import invoice_service.dtos.invoices.requests.InvoiceCreateRequest
import invoice_service.dtos.invoices.requests.InvoiceUpdateRequest
import invoice_service.dtos.invoices.responses.InvoicesPagedResponse
import invoice_service.messaging.MessageSender
import invoice_service.messaging.pendingMessages.PendingInvoiceMessages
import invoice_service.models.invoices.Invoice
import invoice_service.services.InvoiceService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

@Tag(name = "Invoices", description = "API pro správu faktur")
@RestController
@RequestMapping("/api/invoices")
class InvoiceController(
    private val service: InvoiceService,
    private val pendingInvoiceMessages: PendingInvoiceMessages,
    private val messageSender: MessageSender
) {

    @Operation(summary = "Získat všechny faktury", description = "Vrací stránkovaný seznam všech faktur.")
    @GetMapping("/get-invoices-pages")
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
    @GetMapping("/get-by-id/{id}")
    fun getInvoice(
        @Parameter(description = "ID faktury", example = "1")
        @PathVariable id: Long
    ): ResponseEntity<Invoice> =
        service.getInvoice(id)?.let { ResponseEntity.ok(it) } ?: (ResponseEntity.notFound().build())

    @Operation(summary = "Získat fakturu podle ID", description = "Vrací detail faktury podle jejího ID.")

    @GetMapping("/get-by-id/{id}/xml")
    fun getInvoiceXml(
        @Parameter(description = "ID faktury", example = "1")
        @PathVariable id: Long
    ): ResponseEntity<String> {
        val correlationId = UUID.randomUUID().toString()
        val future = CompletableFuture<MessageResponse>()
        pendingInvoiceMessages.registerInvoiceResponseFuture(correlationId, future)
        messageSender.sendInvoiceRequest(
            InvoiceRequest(
                apiSourceService = SourceService.INVOICE,
                requestId = correlationId,
                targetId = id,
                payload = null,
                action = MessageInvoiceAction.RENDER
            ),
            correlationId
        )
        val response = future.get(10, TimeUnit.SECONDS)
        return response.payload?.get("xml")?.let { ResponseEntity.ok(it.toString()) } ?: ResponseEntity.notFound()
            .build()
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
                responseCode = "409",
                description = "Faktura s tímto číslem již existuje",
                content = [Content(schema = Schema(example = "{\"error\": \"Faktura s tímto číslem již existuje.\"}"))]
            )
        ]
    )
    @PostMapping("/create")
    fun createInvoice(
        @Parameter(description = "Data pro vytvoření faktury")
        @RequestBody request: InvoiceCreateRequest
    ): Invoice {
        return service.createInvoice(request)
    }

    @Operation(summary = "Aktualizovat fakturu", description = "Aktualizuje existující fakturu podle ID.")
    @PutMapping("/update/{id}")
    fun updateInvoice(
        @Parameter(description = "ID faktury", example = "1")
        @PathVariable id: Long,
        @Parameter(description = "Data pro aktualizaci faktury")
        @RequestBody request: InvoiceUpdateRequest
    ): ResponseEntity<Invoice> {
        val updated = service.updateInvoice(id, request)
        return if (updated != null) ResponseEntity.ok(updated) else ResponseEntity.status(403).build()
    }

    @Operation(summary = "Smazat fakturu", description = "Smaže fakturu podle ID.")
    @DeleteMapping("/delete/{id}")
    fun deleteInvoice(
        @Parameter(description = "ID faktury", example = "1")
        @PathVariable id: Long
    ): ResponseEntity<String> {
        val deleted = service.deleteInvoice(id)
        return if (deleted) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.status(409).body("Nelze smazat fakturu")
        }
    }
}