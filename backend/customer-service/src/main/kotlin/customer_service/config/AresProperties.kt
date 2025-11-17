package customer_service.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "ares")
data class AresProperties(
    var baseUrl: String = "https://ares.gov.cz/ekonomicke-subjekty-v-be/rest",
    val ekonomickeSubjektyPath: String = "$baseUrl/ekonomicke-subjekty"
)
