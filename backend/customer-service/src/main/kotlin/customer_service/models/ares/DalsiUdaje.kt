package customer_service.models.ares

data class DalsiUdaje(
    val obchodniJmeno: List<ObchodniJmenoUdaje> = emptyList(),
    val sidlo: List<SidloUdaje> = emptyList(),
    val pravniForma: String? = null,
    val spisovaZnacka: String? = null,
    val datovyZdroj: String? = null
)



