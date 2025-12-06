package customer_service.dto.supplier.request

import customer_service.models.Supplier

data class CreateSupplierDto(
    val tradeName: String?,
    val email: String,
    val phoneNumber: String,
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
    fun toEntity(): Supplier {
        return Supplier(
            tradeName = tradeName ?: "",
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
            currency = currency,
        )
    }
}