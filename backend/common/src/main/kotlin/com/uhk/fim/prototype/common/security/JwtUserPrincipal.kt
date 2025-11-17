package com.uhk.fim.prototype.common.security

import org.springframework.security.core.GrantedAuthority

data class JwtUserPrincipal(
    val id: Long,
    val username: String,
    val roles: List<GrantedAuthority>
)
