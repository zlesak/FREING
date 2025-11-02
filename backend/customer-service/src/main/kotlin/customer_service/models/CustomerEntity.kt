package customer_service.models

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "customer")
data class CustomerEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var name: String = "",

    @Column(nullable = false)
    var surname: String = "",

    @Column(nullable = false, unique = true)
    var email: String = "",

    @Column(nullable = false, unique = true)
    var phoneNumber: String = "",

    @Column(nullable = false)
    var birthDate: Date = Date(),

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
    var currency: String? = null,
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "name" to name,
        "surname" to surname,
        "email" to email,
        "phoneNumber" to phoneNumber,
        "birthDate" to birthDate,
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
}
