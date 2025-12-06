package invoice_service.services

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.uhk.fim.prototype.common.exceptions.BadGatewayException
import invoice_service.messaging.handlers.CustomerServiceRequestHandler
import org.mustangproject.*
import org.mustangproject.ZUGFeRD.Profiles
import org.mustangproject.ZUGFeRD.ZUGFeRD2PullProvider
import org.springframework.stereotype.Service
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.time.ZoneId
import java.util.*
import javax.imageio.ImageIO

@Service
class ZugferdService (
    private val invoiceService: InvoiceService,
    private val customerServiceRequestHandler: CustomerServiceRequestHandler
){

    fun createInvoice(invoiceId: Long): String {

        val invoice = invoiceService.getInvoice(invoiceId, true)
        val customer = customerServiceRequestHandler.getCustomerById(invoice.customerId)
        val supplier = customerServiceRequestHandler.getSupplierById(invoice.supplierId)

        val i = Invoice()

        i.setNumber(invoice.invoiceNumber)
            .setReferenceNumber(invoice.referenceNumber)
            .setIssueDate(Date.from(invoice.issueDate.atStartOfDay(ZoneId.systemDefault()).toInstant()))
            .setDueDate(Date.from(invoice.dueDate.atStartOfDay(ZoneId.systemDefault()).toInstant()))
            .setCurrency(invoice.currency)
            .setSender(
                toTradeParty(supplier)
            )
            .setRecipient(
                toTradeParty(customer)
            )

        for (item in invoice.items) {
            i.addItem(
                Item(
                    Product(
                        item.name,
                        item.description,
                        item.unit,
                        item.vatRate
                    ),
                    item.unitPrice,
                    item.quantity,
                )
            )
        }

        try {
            val pageurl = "freing.test/payment/" + invoice.invoiceNumber
            val qr = generateQR(pageurl)
            i.addNote(qr)

            val isStream = ZugferdService::class.java.classLoader.getResourceAsStream("logo.png")
            if (isStream != null) {
                val imageBytes = isStream.readBytes()
                val logoString = Base64.getEncoder().encodeToString(imageBytes)
                i.addNote(logoString)
            }


            val zf2p = ZUGFeRD2PullProvider()
            zf2p.setProfile(Profiles.getByName("BASIC"))
            zf2p.generateXML(i)
            return String(zf2p.xml)

        } catch (e: Exception) {
            throw BadGatewayException("Zugferd invoice processing failed: ${e.message ?: "Unknown error"}")
        }
    }

    private fun generateQR(text: String): String {
        val width = 500
        val height = 500

        val hintMap: MutableMap<EncodeHintType, Any> = HashMap()
        hintMap[EncodeHintType.CHARACTER_SET] = "UTF-8"

        val qrCodeWriter = QRCodeWriter()
        val byteMatrix: BitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height, hintMap)

        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        image.createGraphics()
        val graphics = image.graphics as Graphics2D
        graphics.color = java.awt.Color.WHITE
        graphics.fillRect(0, 0, width, height)
        graphics.color = java.awt.Color.BLACK
        for (i in 0 until height) {
            for (j in 0 until width) {
                if (byteMatrix.get(j, i)) {
                    graphics.fillRect(j, i, 1, 1)
                }
            }
        }

        ByteArrayOutputStream().use { baos ->
            ImageIO.write(image, "png", baos)
            baos.flush()
            val encodedImage = Base64.getEncoder().encodeToString(baos.toByteArray())
            return encodedImage
        }
    }

  fun toTradeParty(party: Map<String, Any>): TradeParty {
        val tradeName = party["tradeName"] as? String
        val name = party["name"] as? String ?: ""
        val surname = party["surname"] as? String ?: ""
        val email = party["email"] as? String ?: ""
        val phoneNumber = party["phoneNumber"] as? String ?: ""
        val street = party["street"] as? String ?: ""
        val houseNumber = party["houseNumber"] as? String ?: ""
        val city = party["city"] as? String ?: ""
        val zip = party["zip"] as? String ?: ""
        val country = party["country"] as? String ?: ""
        val ico = party["ico"] as? String
        val dic = party["dic"] as? String
        val bankCode = party["bankCode"] as? String
        val bankAccount = party["bankAccount"] as? String
        val currency = party["currency"] as? String

        val fullName = tradeName?.takeIf { it.isNotBlank() }
            ?: listOfNotNull(name.takeIf { it.isNotBlank() }, surname.takeIf { it.isNotBlank() }).joinToString(" ")
                .ifBlank { name }

        val streetWithNumber = ("$street $houseNumber").trim()

        val tp = TradeParty(
            fullName,
            streetWithNumber,
            zip.ifBlank { "" },
            city.ifBlank { "" },
            country.ifBlank { "" }
        )

        ico?.takeIf { it.isNotBlank() }?.let { tp.addTaxID(it) }
        dic?.takeIf { it.isNotBlank() }?.let { tp.addVATID(it) }

        tp.setContact(Contact(fullName, phoneNumber, email))

        if (!bankAccount.isNullOrBlank() || !bankCode.isNullOrBlank()) {
            val ibanLike = when {
                !currency.isNullOrBlank() && !bankCode.isNullOrBlank() && !bankAccount.isNullOrBlank() -> "${bankCode}${bankAccount}"
                !bankAccount.isNullOrBlank() -> bankAccount
                else -> bankCode ?: ""
            }
            tp.addBankDetails(BankDetails(ibanLike, ""))
        }

        return tp
    }
}