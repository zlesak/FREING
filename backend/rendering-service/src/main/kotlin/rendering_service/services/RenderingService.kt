package rendering_service.services

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import rendering_service.dto.PdfMetadataDto
import rendering_service.model.Pdf
import rendering_service.repository.PdfRepository
import java.time.LocalDateTime

@Service
class RenderingService(
    private val pdfRepository: PdfRepository
) {
    fun getPdf(id: Long): Pdf? = pdfRepository.findById(id).orElse(null)

    fun deletePdf(id: Long): Boolean {
        if (!pdfRepository.existsById(id)) return false
        pdfRepository.deleteById(id)
        return true
    }

    fun listPdfs(
        type: String?,
        filename: String?,
        generatedFrom: String?,
        generatedTo: String?,
        pageable: Pageable
    ): Page<PdfMetadataDto> {
        var spec: Specification<Pdf>? = null

        if (type != null) {
            val typeSpec = Specification<Pdf> { root, _, cb -> cb.equal(root.get<String>("type"), type) }
            spec = if (spec == null) typeSpec else spec.and(typeSpec)
        }
        if (filename != null) {
            val filenameSpec = Specification<Pdf> { root, _, cb -> cb.like(root.get<String>("filename"), "%$filename%") }
            spec = if (spec == null) filenameSpec else spec.and(filenameSpec)
        }
        if (generatedFrom != null) {
            val parsedFrom = LocalDateTime.parse(generatedFrom)
            val fromSpec = Specification<Pdf> { root, _, cb -> cb.greaterThanOrEqualTo(root.get("generatedAt"), parsedFrom) }
            spec = if (spec == null) fromSpec else spec.and(fromSpec)
        }
        if (generatedTo != null) {
            val parsedTo = LocalDateTime.parse(generatedTo)
            val toSpec = Specification<Pdf> { root, _, cb -> cb.lessThanOrEqualTo(root.get("generatedAt"), parsedTo) }
            spec = if (spec == null) toSpec else spec.and(toSpec)
        }

        val pdfPage = if (spec != null) {
            pdfRepository.findAll(spec, pageable)
        } else {
            pdfRepository.findAll(pageable)
        }
        val metadataList = pdfPage.content.map { pdf ->
            PdfMetadataDto(
                id = pdf.id,
                type = pdf.type,
                generatedAt = pdf.generatedAt,
                filename = pdf.filename
            )
        }

        return PageImpl(metadataList, pageable, pdfPage.totalElements)
    }
}