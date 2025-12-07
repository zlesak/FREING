package rendering_service.services

import com.itextpdf.text.pdf.BaseFont
import org.springframework.stereotype.Service
import org.xhtmlrenderer.pdf.ITextRenderer
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.StringReader
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import javax.xml.transform.OutputKeys
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource
import org.slf4j.LoggerFactory

@Service
class PdfRenderingService {
    private val logger = LoggerFactory.getLogger(PdfRenderingService::class.java)

    fun renderInvoicePdf(invoiceData: Map<String, Any?>): ByteArray {
        val xml = invoiceData["payload"] as? String
            ?: throw IllegalArgumentException("Missing XML data in payload")

        val filename = "invoice.pdf"
        try {
            val tFactory = TransformerFactory.newInstance()
            val xslStream = javaClass.classLoader.getResourceAsStream("xslformater.xsl")
                ?: throw IllegalArgumentException("File formater not found, could not make formated document")
            val transformer: Transformer = tFactory.newTransformer(StreamSource(xslStream))
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8")
            transformer.setOutputProperty(OutputKeys.METHOD, "xml")
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no")

            val sampleHtmlFile = File("sample.html")
            FileOutputStream(sampleHtmlFile).use { fos ->
                transformer.transform(StreamSource(StringReader(xml)), StreamResult(fos))
            }

            val htmlContent = sampleHtmlFile.readText(Charsets.UTF_8)
            logger.debug(htmlContent)

            val fontPath: String = resolveFontPath("fonts/DejaVuSans.ttf")

            val baseUri =
                sampleHtmlFile.parentFile?.toURI()?.toURL()?.toString() ?: File(".").toURI().toURL().toString()

            FileOutputStream(filename).use { os ->
                val renderer = ITextRenderer()

                try {
                    renderer.fontResolver.addFont(fontPath, BaseFont.IDENTITY_H, true)
                } catch (e: Exception) {
                    logger.error("Font load error:", e)
                }

                renderer.setDocumentFromString(htmlContent, baseUri)
                renderer.layout()
                renderer.createPDF(os)
            }

            val pdf = readPdfAsBinaryData(filename)

            Files.deleteIfExists(Paths.get(filename))
            Files.deleteIfExists(Paths.get("sample.html"))

            return pdf
        } catch (e: Exception) {
            logger.error("PDF generating error:", e)
            throw RuntimeException("PDF generating error: ", e)
        }
    }

    private fun resolveFontPath(resourcePath: String): String {
        val cl = javaClass.classLoader
        val url = cl.getResource(resourcePath)
            ?: throw IllegalArgumentException("Font $resourcePath have not been found in resources")

        return try {
            if (url.protocol == "file") {
                Paths.get(url.toURI()).toAbsolutePath().toString()
            } else {
                cl.getResourceAsStream(resourcePath).use { stream ->
                    requireNotNull(stream) { "Resource stream is null: $resourcePath" }
                    val tmp = Files.createTempFile("font-", ".ttf")
                    Files.copy(stream, tmp, StandardCopyOption.REPLACE_EXISTING)
                    tmp.toFile().deleteOnExit()
                    tmp.toAbsolutePath().toString()
                }
            }
        } catch (ex: Exception) {
            throw IOException("Could not get font $resourcePath: ${ex.message}", ex)
        }
    }

    @Throws(IOException::class)
    private fun readPdfAsBinaryData(filename: String): ByteArray {
        val file = File(filename)
        val data = ByteArray(file.length().toInt())
        val fis = FileInputStream(file)
        fis.read(data)
        fis.close()
        return data
    }
}