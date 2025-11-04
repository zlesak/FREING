package customer_service.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.http.HttpMethod
import org.springframework.security.config.Customizer
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.cors(Customizer.withDefaults())
        http
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                    // Allow OpenAPI/Swagger UI and docs to be accessed without auth
                    .requestMatchers(
                        "/v3/api-docs",
                        "/v3/api-docs/**",
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/swagger-ui/index.html",
                        "/swagger-resources/**",
                        "/webjars/**"
                    ).permitAll()
                    .requestMatchers("/api/customers/**").hasAnyAuthority("SCOPE_service.call", "ROLE_manager", "ROLE_accountant")
                    .anyRequest().authenticated()
            }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { jwt ->
                    jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())
                }
            }
        return http.build()
    }

    private fun jwtAuthenticationConverter(): Converter<Jwt, AbstractAuthenticationToken> {
        val converter = JwtAuthenticationConverter()
        converter.setJwtGrantedAuthoritiesConverter { jwt ->
            val authorities = mutableListOf<GrantedAuthority>()

            // map scopes/scp -> SCOPE_x authorities
            val scopesFromString = jwt.claims["scope"] as? String
            if (!scopesFromString.isNullOrBlank()) {
                scopesFromString.split(" ").forEach { authorities.add(SimpleGrantedAuthority("SCOPE_$it")) }
            }
            // some tokens use 'scp' as list
            val scopes = jwt.claims["scp"] as? List<*>
            scopes?.filterIsInstance<String>()?.forEach { authorities.add(SimpleGrantedAuthority("SCOPE_$it")) }

            // map realm roles -> ROLE_x
            val realm = jwt.claims["realm_access"] as? Map<*, *>
            val realmRoles = realm?.get("roles") as? List<*>
            realmRoles?.filterIsInstance<String>()?.forEach { authorities.add(SimpleGrantedAuthority("ROLE_$it")) }

            // map client (resource) roles -> ROLE_x
            val resourceAccess = jwt.claims["resource_access"] as? Map<*, *>
            resourceAccess?.values?.forEach { entry ->
                val map = entry as? Map<*, *>
                val roles = map?.get("roles") as? List<*>
                roles?.filterIsInstance<String>()?.forEach { authorities.add(SimpleGrantedAuthority("ROLE_$it")) }
            }

            authorities
        }
        return converter
    }
}
