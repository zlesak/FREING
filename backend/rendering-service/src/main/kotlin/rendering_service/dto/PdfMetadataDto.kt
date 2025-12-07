package rendering_service.dto

import java.time.LocalDateTime

data class PdfMetadataDto(
    val id: Long,
    val type: String,
    val generatedAt: LocalDateTime,
    val filename: String
)
