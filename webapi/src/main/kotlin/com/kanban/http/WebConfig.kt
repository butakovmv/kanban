package com.kanban.http

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.config.WebFluxConfigurer

/**
 * Конфигурация WebFlux для HTTP-слоя приложения.
 * Настраивает ObjectMapper в стиле snake_case и регистрирует
 * JSON-кодеки для сообщений HTTP.
 */
@Configuration
@EnableWebFlux
class WebConfig : WebFluxConfigurer {
    /**
     * Создаёт и настраивает ObjectMapper для сериализации/десериализации JSON.
     * - Добавляет модуль Kotlin для поддержки data-классов
     * - Устанавливает стратегию именования свойств snake_case
     *
     * @return настроенный экземпляр ObjectMapper
     */
    @Bean
    fun objectMapper(): ObjectMapper =
        jsonMapper {
            addModule(kotlinModule())
            addModule(JavaTimeModule())
            propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        }.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

    /**
     * Настраивает кодеки HTTP-сообщений на использование кастомного ObjectMapper.
     *
     * @param configurer конфигуратор кодеков сервера
     */
    override fun configureHttpMessageCodecs(configurer: ServerCodecConfigurer) {
        val mapper = objectMapper()
        configurer.defaultCodecs().jackson2JsonEncoder(Jackson2JsonEncoder(mapper))
        configurer.defaultCodecs().jackson2JsonDecoder(Jackson2JsonDecoder(mapper))
    }
}
