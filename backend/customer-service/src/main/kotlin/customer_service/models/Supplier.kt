package customer_service.models

import customer_service.dto.customer.response.CustomerDto
import customer_service.dto.supplier.response.SupplierDto
import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "supplier")
data class Supplier(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var tradeName: String = "",

    @Column(nullable = false)
    var email: String = "",

    @Column(nullable = false)
    var phoneNumber: String = "",

    @Column(nullable = false)
    var street: String = "",

    @Column(nullable = false)
    var houseNumber: String = "",

    @Column(nullable = false)
    var city: String = "",

    @Column(nullable = false)
    var zip: String = "",

    @Column(nullable = false)
    var country: String = "",

    @Column(nullable = true)
    var ico: String? = null,

    @Column(nullable = true)
    var dic: String? = null,

    @Column(nullable = true)
    var bankCode: String? = null,

    @Column(nullable = true)
    var bankAccount: String? = null,

    @Column(nullable = true)
    var currency: String? = null
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "email" to email,
        "phoneNumber" to phoneNumber,
        "street" to street,
        "houseNumber" to houseNumber,
        "city" to city,
        "zip" to zip,
        "country" to country,
        "ico" to ico,
        "dic" to dic,
        "bankCode" to bankCode,
        "bankAccount" to bankAccount,
        "currency" to currency
    )

    fun toDto(): SupplierDto {
        return SupplierDto(
            id = id ?: -1,
            tradeName = tradeName,
            email = email,
            phoneNumber = phoneNumber,
            street = street,
            houseNumber = houseNumber,
            city = city,
            zip = zip,
            country = country,
            ico = ico,
            dic = dic,
            bankCode = bankCode,
            bankAccount = bankAccount,
            currency = currency
        )
    }

    fun updateFrom(src: Supplier) {
        if (src.tradeName.isNotBlank()) tradeName = src.tradeName
        if (src.email.isNotBlank()) email = src.email
        if (src.phoneNumber.isNotBlank()) phoneNumber = src.phoneNumber

        street = src.street
        houseNumber = src.houseNumber
        city = src.city
        zip = src.zip
        country = src.country
        ico = src.ico
        dic = src.dic
        bankCode = src.bankCode
        bankAccount = src.bankAccount
        currency = src.currency
    }
}