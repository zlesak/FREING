package customer_service.external

import customer_service.config.AresProperties
import customer_service.models.ares.Subjekt
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class AresClient(
    private val props: AresProperties,
    private val restTemplate: RestTemplate = RestTemplate(),
) : IAresClient {
    override fun getSubjectByIcoARES(ico: String): Subjekt? =
        restTemplate.getForEntity("${props.ekonomickeSubjektyPath}/$ico", Subjekt::class.java).body
}