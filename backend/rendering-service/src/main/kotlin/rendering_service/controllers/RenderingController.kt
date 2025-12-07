package rendering_service.controllers

import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import rendering_service.dto.PdfMetadataDto
import rendering_service.services.RenderingService


@Tag(name = "Rendering", description = "API pro správu renderovaných souborů")
@RestController
@PreAuthorize("hasRole('MANAGER')")
@RequestMapping("/api/rendering")
class RenderingController(
    private val renderingService: RenderingService
) {
    @GetMapping("/{id}")
    fun getPdf(@PathVariable id: Long): ResponseEntity<ByteArray> {
        val pdf = renderingService.getPdf(id)
            ?: return ResponseEntity.notFound().build()
        val headers = HttpHeaders()
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${pdf.filename}\"")
        headers.add(HttpHeaders.CONTENT_TYPE, "application/pdf")
        return ResponseEntity.ok().headers(headers).body(pdf.pdfData)
    }

    @DeleteMapping("/{id}")
    fun deletePdf(@PathVariable id: Long): ResponseEntity<Void> {
        val deleted = renderingService.deletePdf(id)
        return if (deleted) ResponseEntity.noContent().build() else ResponseEntity.notFound().build()
    }

    @GetMapping
    fun listPdfs(
        @RequestParam(required = false) type: String?,
        @RequestParam(required = false) filename: String?,
        @RequestParam(required = false) generatedFrom: String?,
        @RequestParam(required = false) generatedTo: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(required = false) sort: List<String>?
    ): Page<PdfMetadataDto> {
        val validSortFields = setOf("id", "type", "generatedAt", "filename")

        val pageable = if (sort != null && sort.isNotEmpty()) {
            val validSortOrders = sort.mapNotNull { sortParam ->
                val parts = sortParam.split(",")
                val field = parts[0]
                val direction = if (parts.size > 1) parts[1] else "asc"

                if (validSortFields.contains(field)) {
                    if (direction.lowercase() == "desc") {
                        Sort.Order.desc(field)
                    } else {
                        Sort.Order.asc(field)
                    }
                } else null
            }

            if (validSortOrders.isNotEmpty()) {
                PageRequest.of(page, size, Sort.by(validSortOrders))
            } else {
                PageRequest.of(page, size, Sort.by("generatedAt").descending())
            }
        } else {
            PageRequest.of(page, size, Sort.by("generatedAt").descending())
        }
        return renderingService.listPdfs(type, filename, generatedFrom, generatedTo, pageable)
    }
}