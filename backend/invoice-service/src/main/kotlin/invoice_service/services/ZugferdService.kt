package invoice_service.services

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.uhk.fim.prototype.common.exceptions.BadGatewayException
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
class ZugferdService {

    fun createInvoice(invoice: invoice_service.models.invoices.Invoice, customer: Map<String, Any>): String {

        val i = Invoice()

        i.setNumber(invoice.invoiceNumber)
            .setReferenceNumber(invoice.referenceNumber)
            .setIssueDate(Date.from(invoice.issueDate.atStartOfDay(ZoneId.systemDefault()).toInstant()))
            .setDueDate(Date.from(invoice.dueDate.atStartOfDay(ZoneId.systemDefault()).toInstant()))
            .setCurrency(invoice.currency)
            .setSender(
                getSupplierTradeParty()
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

    private fun getSupplierTradeParty(): TradeParty {
        val fullName = "Default Supplier Name"
        val streetWithNumber = ("Národní 15").trim()

        val tp = TradeParty(
            fullName,
            streetWithNumber,
            "500 03",
            "Hradec Králové",
            "Czech Republic"
        )
            .addTaxID("Danove cislo ICO")
            .addVATID("Danove cislo DIC")
            .setContact(Contact(fullName, "+420 700 000 000", "test@example.com"))
            .addBankDetails(BankDetails("CZ7450515657766535784736", ""))

        return tp
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

    fun toTradeParty(customer: Map<String, Any>): TradeParty {
        val name = customer["name"] as? String ?: ""
        val surname = customer["surname"] as? String ?: ""
        val email = customer["email"] as? String ?: ""
        val phoneNumber = customer["phoneNumber"] as? String ?: ""
        val street = customer["street"] as? String ?: ""
        val houseNumber = customer["houseNumber"] as? String ?: ""
        val city = customer["city"] as? String ?: ""
        val zip = customer["zip"] as? String ?: ""
        val country = customer["country"] as? String ?: ""
        val ico = customer["ico"] as? String
        val dic = customer["dic"] as? String
        val bankCode = customer["bankCode"] as? String
        val bankAccount = customer["bankAccount"] as? String
        val currency = customer["currency"] as? String
        val fullName =
            listOfNotNull(name.takeIf { it.isNotBlank() }, surname.takeIf { it.isNotBlank() }).joinToString(" ")
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