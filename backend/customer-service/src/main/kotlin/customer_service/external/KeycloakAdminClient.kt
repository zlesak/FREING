package customer_service.external

import jakarta.ws.rs.core.Response
import org.keycloak.admin.client.Keycloak
import org.keycloak.representations.idm.CredentialRepresentation
import org.keycloak.representations.idm.UserRepresentation
import org.springframework.stereotype.Service
@Service
class KeycloakAdminClient(private val keycloak: Keycloak) {

    fun createUser(user: UserRepresentation, password: String): Int {
        val realmResource = keycloak.realm("freing")
        val usersResource = realmResource.users()

        val response = usersResource.create(user)
        val statusCode = response.status

        if (statusCode == 201) {
            val userId = response.location.path.split("/").last()
            val passwordCred = CredentialRepresentation().apply {
                isTemporary = false
                type = CredentialRepresentation.PASSWORD
                value = password
            }
            usersResource.get(userId).resetPassword(passwordCred)
        }
        return statusCode
    }
}

