package customer_service.dto.request

import customer_service.models.CustomerEntity
import java.util.*

data class CreateCustomerDto(
    val name: String,
    val surname: String,
    val email: String,
    val phoneNumber: String,
    val birthDate: Date,
    val street: String,
    val houseNumber: String,
    val city: String,
    val zip: String,
    val country: String,
    val ico: String? = null,
    val dic: String? = null,
    val bankCode: String? = null,
    val bankAccount: String? = null,
    val currency: String? = null,
) {
    fun toEntity(): CustomerEntity {
        return CustomerEntity(
            name = name,
            surname = surname,
            email = email,
            phoneNumber = phoneNumber,
            birthDate = birthDate,
            street = street,
            houseNumber = houseNumber,
            city = city,
            zip = zip,
            country = country,
            ico = ico,
            dic = dic,
            bankCode = bankCode,
            bankAccount = bankAccount,
            currency = currency,
        )
    }
}