package customer_service.dto.supplier.response

import customer_service.dto.common.SubjectDto
import customer_service.models.Supplier

data class SupplierDto(
    override val id: Long,
    override val tradeName: String,
    override val email: String,
    override val phoneNumber: String,
    val street: String,
    val houseNumber: String,
    val city: String,
    val zip: String,
    val country: String,
    val ico: String? = null,
    val dic: String? = null,
    val bankCode: String? = null,
    val bankAccount: String? = null,
    val currency: String? = null
) : SubjectDto
{
    fun toEntity(): Supplier {
        return Supplier(
            id = id,
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
}
