package customer_service.dto.response

import customer_service.models.CustomerEntity
import java.util.Date

data class CustomerDto(
    val id: Long? = null,
    val name: String,
    val surname: String,
    val email: String,
    val phoneNumber: String,
    val birthDate: Date,
)

fun CustomerDto.toEntity(): CustomerEntity {
    return CustomerEntity(
        id = id,
        name = name,
        surname = surname,
        email = email,
        phoneNumber = phoneNumber,
        birthDate = birthDate,
    )
}

fun CustomerEntity.toDto(): CustomerDto {
    return CustomerDto(
        id = id,
        name = name,
        surname = surname,
        email = email,
        phoneNumber = phoneNumber,
        birthDate = birthDate,
    )
}
