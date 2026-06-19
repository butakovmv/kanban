package com.kanban

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource

/**
 * Конфигурация безопасности приложения.
 * Настраивает цепочку фильтров безопасности WebFlux
 * и политику CORS для кросс-доменных запросов.
 */
@Configuration
@EnableWebFluxSecurity
class SecurityConfig {
    /**
     * Создаёт основную цепочку фильтров безопасности.
     * - Отключает CSRF-защиту
     * - Разрешает доступ к /actuator/health без аутентификации
     * - Разрешает все остальные запросы
     * - Применяет CORS-конфигурацию
     *
     * @param http конфигуратор безопасности HTTP
     * @return построенная цепочка фильтров SecurityWebFilterChain
     */
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

    /**
     * Создаёт источник CORS-конфигурации, разрешающий
     * запросы с любых источников, любых заголовков
     * и указанных HTTP-методов с поддержкой credentials.
     *
     * @return настроенный UrlBasedCorsConfigurationSource
     */
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
