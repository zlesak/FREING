package rendering_service

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType1Font
import java.io.ByteArrayOutputStream

class PdfRenderingService {
    fun renderInvoicePdf(invoiceData: Map<String, Any>): ByteArray {
        val document = PDDocument()
        val page = PDPage(PDRectangle.A4)
        document.addPage(page)

        PDPageContentStream(document, page).use { contentStream ->
            contentStream.beginText()
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18f)
            contentStream.newLineAtOffset(50f, 750f)
            contentStream.showText("Faktura")
            contentStream.endText()

            // TODO: udělat implementaci lepší, tohle jenom na ukázku funkcionality
            var y = 720f
            contentStream.setFont(PDType1Font.HELVETICA, 12f)
            for ((key, value) in invoiceData) {
                contentStream.beginText()
                contentStream.newLineAtOffset(50f, y)
                contentStream.showText("$key: $value")
                contentStream.endText()
                y -= 20f
            }
        }

        val out = ByteArrayOutputStream()
        document.save(out)
        document.close()
        return out.toByteArray()
    }
}

