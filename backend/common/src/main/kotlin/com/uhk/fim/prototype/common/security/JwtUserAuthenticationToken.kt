package com.uhk.fim.prototype.common.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.AbstractOAuth2TokenAuthenticationToken

class JwtUserAuthenticationToken(
    private val principal: JwtUserPrincipal,
    jwt: Jwt,
    authorities: Collection<GrantedAuthority>
) : AbstractOAuth2TokenAuthenticationToken<Jwt>(jwt, authorities) {

    init {
        super.setAuthenticated(true)
    }

    override fun getCredentials(): Any? = null

    override fun getPrincipal(): JwtUserPrincipal = principal

    override fun getTokenAttributes(): MutableMap<String, Any> = token.claims.toMutableMap()
    override fun getName(): String = principal.username
    fun getUserId(): Long = principal.id
}
