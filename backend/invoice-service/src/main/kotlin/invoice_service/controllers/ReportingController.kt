package invoice_service.controllers

import invoice_service.dtos.reports.requests.InvoiceReportRequest
import invoice_service.dtos.reports.responses.AggregatedReportResponse
import invoice_service.models.invoices.Invoice
import invoice_service.services.ReportingSubService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@Tag(name = "Reporting", description = "Endpoints pro generování a export reportů")
@RestController
@RequestMapping("/api/invoices")
class ReportingController(private val reportingService: ReportingSubService) {

    @Operation(
        summary = "Vytvořit agregovaný report",
        description = "Vygeneruje souhrnný report pro všechny nebo filtrované faktury. " +
                "Pro ochranu databáze je maximální počet faktur v reportu omezen na 10 000."
    )
    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping("/report")
    fun makeAggregatedReport(@RequestBody request: InvoiceReportRequest): AggregatedReportResponse =
        reportingService.makeAggregatedReportByFilter(request)

    @Operation(
        summary = "Exportovat report jako CSV",
        description = "Vygeneruje CSV soubor s reportem podle filtru a vrátí jej ke stažení. " +
                "Pro ochranu databáze je maximální počet faktur v reportu omezen na 10 000."
    )
    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping("/report/csv", produces = ["text/csv;charset=UTF-8"])
    fun exportAggregatedReportCsv(@RequestBody request: InvoiceReportRequest): ResponseEntity<ByteArray> =
        reportingService.generateAggregatedReportCsv(request).let { csvString ->
            ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice-report.csv")
                .body(csvString.toByteArray(Charsets.UTF_8))
        }

    @Operation(
        summary = "Získat filtrované faktury",
        description = "Vrátí stránkovaný seznam faktur odpovídajících zadaným filtrům."
    )
    @PostMapping("/filter")
    fun getFilteredInvoices(
        @RequestBody request: InvoiceReportRequest,
        @Parameter(description = "Číslo stránky", example = "0")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Velikost stránky", example = "10")
        @RequestParam(defaultValue = "10") size: Int
    ): Page<Invoice> =
        reportingService.getFilteredInvoicesPaged(request, PageRequest.of(page, size))

}
