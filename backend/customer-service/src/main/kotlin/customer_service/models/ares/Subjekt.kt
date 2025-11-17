package customer_service.models.ares

import customer_service.models.Customer

data class Subjekt(
    val ico: String? = null,
    val obchodniJmeno: String? = null,
    val sidlo: Sidlo? = null,
    val pravniForma: String? = null,
    val financniUrad: String? = null,
    val datumVzniku: String? = null,
    val datumZaniku: String? = null,
    val datumAktualizace: String? = null,
    val dic: String? = null,
    val icoId: String? = null,
    val adresaDorucovaci: AdresaDorucovaci? = null,
    val seznamRegistraci: SeznamRegistraci? = null,
    val primarniZdroj: String? = null,
    val dalsiUdaje: List<DalsiUdaje>? = null,
    val czNace: List<String>? = null,
    val subRegistrSzr: String? = null,
    val dicSkDph: String? = null
) {
    fun toCustomerEntity(): Customer {
        return Customer(
            name = "",
            surname = "",
            email = "",
            tradeName = obchodniJmeno ?: "",
            phoneNumber = "",
            street = sidlo?.nazevUlice ?: "",
            houseNumber = sidlo?.cisloDomovni?.toString() ?: "",
            city = sidlo?.nazevObce ?: "",
            zip = sidlo?.psc?.toString() ?: "",
            country = sidlo?.nazevStatu ?: "",
            ico = ico ?: "",
            dic = dic ?: "",
            bankCode = null,
            bankAccount = null,
            currency = null
        )
    }
}
