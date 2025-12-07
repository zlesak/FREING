package rendering_service.model

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Lob
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "pdf")
data class Pdf(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,
    val type: String,
    val generatedAt: LocalDateTime,
    val filename: String,
    @Lob
    val pdfData: ByteArray,
    @Lob
    val xmlData: String
) {
    constructor() : this(
        id = 0L,
        type = "",
        generatedAt = LocalDateTime.now(),
        filename = "",
        pdfData = ByteArray(0),
        xmlData = ""
    )
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Pdf

        if (id != other.id) return false
        if (type != other.type) return false
        if (generatedAt != other.generatedAt) return false
        if (filename != other.filename) return false
        if (!pdfData.contentEquals(other.pdfData)) return false
        if (xmlData != other.xmlData) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + generatedAt.hashCode()
        result = 31 * result + filename.hashCode()
        result = 31 * result + pdfData.contentHashCode()
        result = 31 * result + xmlData.hashCode()
        return result
    }
}
