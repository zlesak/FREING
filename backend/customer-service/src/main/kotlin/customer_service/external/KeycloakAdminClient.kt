package customer_service.external

import org.keycloak.admin.client.Keycloak
import org.keycloak.representations.idm.UserRepresentation
import org.springframework.stereotype.Service

@Service
class KeycloakAdminClient(private val keycloak: Keycloak) {

    fun createUser(user: UserRepresentation): String {
        val realm = keycloak.realm("freing")
        val users = realm.users()

        val response = users.create(user)

        if (response.status != jakarta.ws.rs.core.Response.Status.CREATED.statusCode) {
            throw RuntimeException("Failed to create user in Keycloak. Status: ${response.statusInfo.reasonPhrase}")
        }

        val location = response.location ?: throw RuntimeException("Failed to get user location from Keycloak response")
        return location.path.substringAfterLast('/')
    }

    fun sendUpdatePasswordEmail(userId: String) {
        val realm = keycloak.realm("freing")
        realm.users().get(userId).executeActionsEmail(listOf("UPDATE_PASSWORD"))
    }

    fun addRealmRoleToUser(userId: String, roleName: String) {
        val realm = keycloak.realm("freing")
        val roleRepresentation = realm.roles().get(roleName).toRepresentation()
        realm.users().get(userId).roles().realmLevel().add(listOf(roleRepresentation))
    }

    fun deleteUser(userId: String) {
        val realm = keycloak.realm("freing")
        realm.users().get(userId).remove()
    }
}
