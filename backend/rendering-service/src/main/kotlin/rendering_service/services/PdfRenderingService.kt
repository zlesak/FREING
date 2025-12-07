package rendering_service.services

import com.itextpdf.text.pdf.BaseFont
import com.itextpdf.text.pdf.PdfFileSpecification
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.PdfStamper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.xhtmlrenderer.pdf.ITextRenderer
import rendering_service.components.ReportPieChartGenerator
import rendering_service.model.Pdf
import rendering_service.repository.PdfRepository
import java.io.*
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.time.LocalDateTime
import javax.xml.transform.OutputKeys
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource

@Service
class PdfRenderingService (
    private val pdfRepository: PdfRepository
) {
    private val logger = LoggerFactory.getLogger(PdfRenderingService::class.java)

    fun renderInvoicePdf(xml: String): ByteArray = renderPdf(xml, "invoice.pdf", "xslformater.xsl", injectChart = false)

    fun renderReportPdf(xml: String): ByteArray  {
        val renderedPdf = renderPdf(xml, "report.pdf", "xslformater_report.xsl", injectChart = true)
        val report = Pdf(
            0L,
            "AGGREGATED",
            LocalDateTime.now(),
            "report_${LocalDateTime.now()}.pdf",
            renderedPdf,
            xml
        )
        pdfRepository.save(report)
        return renderedPdf
    }

    private fun injectReportChart(htmlContent: String, xml: String): String {
        val doc = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder()
            .parse(org.xml.sax.InputSource(StringReader(xml)))

        val perCustomerNodes = doc.getElementsByTagName("perCustomer")
        val customerNames = mutableListOf<String>()
        val invoiceCounts = mutableListOf<Double>()
        for (i in 0 until perCustomerNodes.length) {
            val node = perCustomerNodes.item(i)
            val nameNode = node.childNodes
            var name = ""
            var count = 0.0
            for (j in 0 until nameNode.length) {
                val child = nameNode.item(j)
                when (child.nodeName) {
                    "customerName" -> name = child.textContent
                    "invoiceCount" -> count = child.textContent.toDoubleOrNull() ?: 0.0
                }
            }
            if (name.isNotBlank()) {
                customerNames.add(name)
                invoiceCounts.add(count)
            }
        }
        val chartCustomerBase64 = ReportPieChartGenerator.generatePieChartBase64(customerNames, invoiceCounts)

        val statusCounts = mutableMapOf<String, Int>()
        val invoiceNodes = doc.getElementsByTagName("invoices")
        for (i in 0 until invoiceNodes.length) {
            val node = invoiceNodes.item(i)
            val statusNode = node.childNodes
            for (j in 0 until statusNode.length) {
                val child = statusNode.item(j)
                if (child.nodeName == "status") {
                    val status = child.textContent
                    statusCounts[status] = statusCounts.getOrDefault(status, 0) + 1
                }
            }
        }
        val chartStatusBase64 =
            rendering_service.components.ReportStatusPieChartGenerator.generateStatusPieChartBase64(statusCounts)

        val monthCounts = mutableMapOf<String, Int>()
        for (i in 0 until invoiceNodes.length) {
            val node = invoiceNodes.item(i)
            val issueDateNodes = mutableListOf<String>()
            for (j in 0 until node.childNodes.length) {
                val child = node.childNodes.item(j)
                if (child.nodeName == "issueDate") {
                    issueDateNodes.add(child.textContent)
                }
            }
            if (issueDateNodes.size >= 2) {
                val year = issueDateNodes[0]
                val month = issueDateNodes[1]
                val key = "$year-$month"
                monthCounts[key] = monthCounts.getOrDefault(key, 0) + 1
            }
        }
        val chartMonthBase64 =
            rendering_service.components.ReportMonthlyBarChartGenerator.generateMonthlyBarChartBase64(monthCounts)

        val monthIssued = mutableMapOf<String, Int>()
        val monthPaid = mutableMapOf<String, Int>()
        for (i in 0 until invoiceNodes.length) {
            val node = invoiceNodes.item(i)
            val issueDateNodes = mutableListOf<String>()
            var status = ""
            for (j in 0 until node.childNodes.length) {
                val child = node.childNodes.item(j)
                if (child.nodeName == "issueDate") {
                    issueDateNodes.add(child.textContent)
                }
                if (child.nodeName == "status") {
                    status = child.textContent
                }
            }
            if (issueDateNodes.size >= 2) {
                val year = issueDateNodes[0]
                val month = issueDateNodes[1]
                val key = "$year-$month"
                monthIssued[key] = monthIssued.getOrDefault(key, 0) + 1
                if (status == "PAID") {
                    monthPaid[key] = monthPaid.getOrDefault(key, 0) + 1
                }
            }
        }
        val chartLineBase64 =
            rendering_service.components.ReportMonthlyLineChartGenerator.generateMonthlyLineChartBase64(
                monthIssued,
                monthPaid
            )

        val gridHtml = """
        <table style='width:100%;table-layout:fixed;border-collapse:collapse;'>
            <tr>
                <td><img src='data:image/png;base64,$chartCustomerBase64' style='width:100%'/></td>
                <td><img src='data:image/png;base64,$chartStatusBase64' style='width:100%'/></td>
            </tr><tr>
                <td><img src='data:image/png;base64,$chartMonthBase64' style='width:100%'/></td>
                <td><img src='data:image/png;base64,$chartLineBase64' style='width:100%'/></td>
            </tr>
        </table>
        """
        return htmlContent.replace(
            "<h3 style=\"page-break-before: always;\">Souhrn podle zákazníka</h3>",
            "<h2 style=\"page-break-before: always;\">Rychlé grafy</h2>$gridHtml<h3 style=\"page-break-before: always;\">Souhrn podle zákazníka</h3>"
        )
    }

    private fun renderPdf(
        xml: String,
        filename: String,
        xsltTemplateName: String,
        injectChart: Boolean = false
    ): ByteArray {
        try {
            val tFactory = TransformerFactory.newInstance()
            val xslStream = javaClass.classLoader.getResourceAsStream(xsltTemplateName)
                ?: throw IllegalArgumentException("File $xsltTemplateName not found, could not make formatted document")
            val transformer: Transformer = tFactory.newTransformer(StreamSource(xslStream))
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8")
            transformer.setOutputProperty(OutputKeys.METHOD, "xml")
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no")

            val sampleHtmlFile = File("sample_${filename.removeSuffix(".pdf")}.html")
            FileOutputStream(sampleHtmlFile).use { fos ->
                transformer.transform(StreamSource(StringReader(xml)), StreamResult(fos))
            }

            var htmlContent = sampleHtmlFile.readText(Charsets.UTF_8)
            if (injectChart) {
                htmlContent = injectReportChart(htmlContent, xml)
            }
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

            val pdfBytesWithAttachment = attachXmlToPdf(filename, xml)

            Files.deleteIfExists(Paths.get(filename))
            Files.deleteIfExists(Paths.get(sampleHtmlFile.name))
            return pdfBytesWithAttachment
        } catch (e: Exception) {
            logger.error("PDF generating error:", e)
            throw RuntimeException("PDF generating error: ", e)
        }
    }

    private fun attachXmlToPdf(pdfFile: String, xml: String): ByteArray {
        val outputStream = ByteArrayOutputStream()
        val reader = PdfReader(pdfFile)
        val stamper = PdfStamper(reader, outputStream)
        val xmlBytes = xml.toByteArray(Charsets.UTF_8)
        val fileSpec = PdfFileSpecification.fileEmbedded(
            stamper.writer, null, "data.xml", xmlBytes
        )
        stamper.addFileAttachment("XML data", fileSpec)
        stamper.close()
        reader.close()
        return outputStream.toByteArray()
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