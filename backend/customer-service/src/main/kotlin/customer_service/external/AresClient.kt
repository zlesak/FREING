package customer_service.external

import customer_service.config.AresProperties
import customer_service.models.ares.Subjekt
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class AresClient(
    private val props: AresProperties,
) : IAresClient {

    private val restTemplate = RestTemplate()
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun getSubjectByIcoARES(ico: String): Subjekt? {
        val url = "${props.baseUrl}/ekonomicke-subjekty/$ico"

        try {
            val responseEntity = restTemplate.getForEntity(url, Subjekt::class.java)
            if (responseEntity.statusCode.value() == 200) {
                return responseEntity.body
            } else {
                logger.error("Ares API call selhal, ICO: $ico, status: ${responseEntity.statusCode.value()}")
                throw RuntimeException("Ares API call selhal se stavem: ${responseEntity.statusCode.value()}")
            }
        } catch (e: Exception) {
            logger.error("Ares API call selhal pro ICO: $ico", e)
            throw e
        }
    }
}