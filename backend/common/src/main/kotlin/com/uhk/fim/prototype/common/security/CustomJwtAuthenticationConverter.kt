package com.uhk.fim.prototype.common.security

import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt

class CustomJwtAuthenticationConverter : Converter<Jwt, AbstractAuthenticationToken> {

    override fun convert(jwt: Jwt): AbstractAuthenticationToken {
        val authorities = mutableListOf<GrantedAuthority>()

        val scopesFromString = jwt.claims["scope"] as? String
        if (!scopesFromString.isNullOrBlank()) {
            scopesFromString.split(" ").forEach { authorities.add(SimpleGrantedAuthority("SCOPE_$it")) }
        }
        // some tokens use 'scp' as list
        val scopes = jwt.claims["scp"] as? List<*>
        scopes?.filterIsInstance<String>()?.forEach { authorities.add(SimpleGrantedAuthority("SCOPE_$it")) }

        val realm = jwt.claims["realm_access"] as? Map<*, *>
        val realmRoles = realm?.get("roles") as? List<*>
        realmRoles?.filterIsInstance<String>()
            ?.forEach { authorities.add(SimpleGrantedAuthority("ROLE_${it.uppercase()}")) }

        val resourceAccess = jwt.claims["resource_access"] as? Map<*, *>
        resourceAccess?.values?.forEach { entry ->
            val map = entry as? Map<*, *>
            val roles = map?.get("roles") as? List<*>
            roles?.filterIsInstance<String>()
                ?.forEach { authorities.add(SimpleGrantedAuthority("ROLE_${it.uppercase()}")) }
        }

        val idLong = when (val rawId = jwt.claims["db_id"]) {
            is Int -> rawId.toLong()
            is Long -> rawId
            is String -> rawId.toLong()
            else -> 0
        }
        val username = jwt.claims["preferred_username"] as? String
            ?: throw IllegalArgumentException("JWT token neobsahuje preferred_username")
        val roleNames = authorities.filter { it.authority.startsWith("ROLE_") }
        val principal = JwtUserPrincipal(
            id = idLong,
            username = username,
            roles = roleNames
        )

        return JwtUserAuthenticationToken(principal, jwt, authorities)
    }
}