package invoice_service.controller

import invoice_service.dto.InvoiceReportRequest
import invoice_service.dto.AggregatedReportResponse
import invoice_service.service.ReportingSubService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Reporting", description = "Endpoints pro generování a export reportů")
@RestController
@RequestMapping("/api/invoices")
class ReportingController(private val reportingService: ReportingSubService) {

    @Operation(summary = "Vytvořit agregovaný report", description = "Vygeneruje souhrnný report pro všechny nebo filtrované faktury.")
    @PostMapping("/report")
    fun makeAggregatedReport(@RequestBody request: InvoiceReportRequest): ResponseEntity<AggregatedReportResponse> {
        val aggregated = reportingService.makeAggregatedReportByFilter(request)
        return ResponseEntity.ok(aggregated)
    }

    @Operation(summary = "Exportovat report jako CSV", description = "Vygeneruje CSV soubor s reportem podle filtru a vrátí jej ke stažení.")
    @PostMapping("/report/csv")
    fun exportAggregatedReportCsv(@RequestBody request: InvoiceReportRequest): ResponseEntity<ByteArray> {
        val csvBytes = reportingService.generateAggregatedReportCsv(request)
        val headers = HttpHeaders()
        headers.contentType = MediaType.parseMediaType("text/csv; charset=UTF-8")
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice-report.csv")
        return ResponseEntity.ok().headers(headers).body(csvBytes)
    }
}
