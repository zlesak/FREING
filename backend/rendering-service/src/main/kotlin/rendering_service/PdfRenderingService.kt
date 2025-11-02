package rendering_service

import com.itextpdf.text.pdf.BaseFont
import org.xhtmlrenderer.pdf.ITextRenderer
import java.io.*
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import javax.xml.transform.OutputKeys
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource


class PdfRenderingService {
    fun renderInvoicePdf(invoiceData: Map<String, Any>): ByteArray {
        val xml = invoiceData["xml"] as? String
            ?: throw IllegalArgumentException("Chybí XML data v invoiceData")

        val filename = "invoice.pdf"
        try {
            val tFactory = TransformerFactory.newInstance()
            val xslStream = javaClass.classLoader.getResourceAsStream("xslformater.xsl")
                ?: throw IllegalArgumentException("Soubor xslformater.xsl nebyl nalezen v resources")
            val transformer: Transformer = tFactory.newTransformer(StreamSource(xslStream))
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8")
            transformer.setOutputProperty(OutputKeys.METHOD, "xml")
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no")

            val sampleHtmlFile = File("sample.html")
            FileOutputStream(sampleHtmlFile).use { fos ->
                transformer.transform(StreamSource(StringReader(xml)), StreamResult(fos))
            }

            val htmlContent = sampleHtmlFile.readText(Charsets.UTF_8)
            println(htmlContent)

            val fontPath: String = resolveFontPath("fonts/DejaVuSans.ttf")

            val baseUri =
                sampleHtmlFile.parentFile?.toURI()?.toURL()?.toString() ?: File(".").toURI().toURL().toString()

            FileOutputStream(filename).use { os ->
                val renderer = ITextRenderer()

                try {
                    renderer.fontResolver.addFont(fontPath, BaseFont.IDENTITY_H, true)
                } catch (e: Exception) {
                    println("Chyba při načítání fontu:")
                    e.printStackTrace()
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
            e.printStackTrace()
            throw RuntimeException("Chyba při generování PDF", e)
        }
    }

    private fun resolveFontPath(resourcePath: String): String {
        val cl = javaClass.classLoader
        val url = cl.getResource(resourcePath)
            ?: throw IllegalArgumentException("Font $resourcePath nebyl nalezen v resources")

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
            throw IOException("Nelze získat font $resourcePath: ${ex.message}", ex)
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
