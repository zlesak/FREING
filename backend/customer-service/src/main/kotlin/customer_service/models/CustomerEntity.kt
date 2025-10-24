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
)
