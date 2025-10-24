package customer_service.dto.request

import customer_service.dto.response.CustomerDto
import customer_service.models.CustomerEntity
import java.util.Date

data class CreateCustomerDto(
    val name: String,
    val surname: String,
    val email: String,
    val phoneNumber: String,
    val birthDate: Date,
)

fun CreateCustomerDto.toEntity(): CustomerEntity {
    return CustomerEntity(
        name = name,
        surname = surname,
        email = email,
        phoneNumber = phoneNumber,
        birthDate = birthDate,
    )
}