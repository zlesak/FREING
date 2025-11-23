package customer_service.config

import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.KeycloakBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class KeycloakAdminConfig {

    @Value("\${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private lateinit var issuerUri: String

    @Value("\${keycloak.realm}")
    private lateinit var realm: String

    @Value("\${spring.security.oauth2.client.registration.service-client.client-id}")
    private lateinit var clientId: String

    @Value("\${spring.security.oauth2.client.registration.service-client.client-secret}")
    private lateinit var clientSecret: String

    @Value("\${spring.security.oauth2.client.registration.service-client.authorization-grant-type}")
    private lateinit var grantType: String

    @Bean
    fun keycloak(): Keycloak {
        val serverUrl = issuerUri.substring(0, issuerUri.indexOf("/realms"))
        return KeycloakBuilder.builder()
            .serverUrl(serverUrl)
            .realm(realm)
            .clientId(clientId)
            .clientSecret(clientSecret)
            .grantType(grantType)
            .build()
    }
}

