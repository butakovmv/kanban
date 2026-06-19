package com.kanban

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebFluxSecurity
class SecurityConfig {
    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        val spec =
            http
                .csrf { it.disable() }
                .authorizeExchange { exchanges ->
                    exchanges
                        .pathMatchers("/actuator/health")
                        .permitAll()
                    exchanges
                        .anyExchange()
                        .permitAll()
                }
        spec.cors { it.configurationSource(corsConfigSource()) }
        return spec.build()
    }

    private fun corsConfigSource(): UrlBasedCorsConfigurationSource {
        val source = UrlBasedCorsConfigurationSource()
        val config =
            CorsConfiguration()
                .apply {
                    allowedOriginPatterns = listOf("*")
                    allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                    allowedHeaders = listOf("*")
                    allowCredentials = true
                }
        source.registerCorsConfiguration("/**", config)
        return source
    }
}
