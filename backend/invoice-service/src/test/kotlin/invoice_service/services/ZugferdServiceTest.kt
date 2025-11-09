package invoice_service.services

import invoice_service.models.invoices.Invoice
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import java.util.Collections.emptyList

class ZugferdServiceTest {
    private val service = ZugferdService()

    @Test
    fun `Trade party correct value saving and mapping`() {
        val customer = mapOf<String, Any>(
            "name" to "John",
            "surname" to "Doe",
            "email" to "john@example.com",
            "phoneNumber" to "+420123456789",
            "street" to "Main",
            "houseNumber" to "1",
            "city" to "Town",
            "zip" to "12345",
            "country" to "Czech Republic",
            "ico" to "12345678",
            "dic" to "CZ12345678",
            "bankCode" to "CZ",
            "bankAccount" to "000111222",
            "currency" to "CZK"
        )

        val tp = service.toTradeParty(customer)
        assertNotNull(tp)
        assertTrue(tp.name.contains("John Doe"))
        assertTrue(tp.contact.eMail.contains("john@example.com"))
        assertTrue(tp.contact.phone.contains("+420123456789"))
        assertTrue(tp.street.contains("Main 1"))
        assertTrue(tp.location.contains("Town"))
        assertTrue(tp.zip.contains("12345"))
        assertTrue(tp.country.contains("Czech Republic"))
        assertTrue(tp.taxID.contains("12345678"))
        assertTrue(tp.vatID.contains("CZ12345678"))
        assertTrue(tp.bankDetails.first().iban.contains("CZ000111222"))
    }

    @Test
    fun `Create invoice xml generated`() {
        val inv = Invoice()
        inv.id = 1L
        inv.invoiceNumber = "INV-TEST"
        inv.referenceNumber = "REF"
        inv.issueDate = java.time.LocalDate.now()
        inv.dueDate = java.time.LocalDate.now()
        inv.currency = "CZK"
        inv.items = emptyList()

        val customer = mapOf<String, Any>("name" to "X")

        val xml = service.createInvoice(inv, customer)
        assertNotNull(xml)
        assertTrue(xml.isNotBlank())
    }
}

