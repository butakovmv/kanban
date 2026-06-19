package com.kanban.http.auth

import com.kanban.identity.AuthHandler
import java.util.UUID

/**
 * Генератор тестовых DTO для auth-запросов.
 * Создаёт случайные данные, которые используются в тестах контроллеров.
 */
internal object RequestGenerator {
    fun registerRequest(): AuthHandler.RegisterRequest =
        AuthHandler.RegisterRequest(
            email = "user-${UUID.randomUUID().toString().take(8)}@kanban.test",
            password = "secure-pwd-${UUID.randomUUID().toString().take(6)}",
            displayName = "Test User ${UUID.randomUUID().toString().take(6)}",
        )

    fun loginRequest(): AuthHandler.LoginRequest =
        AuthHandler.LoginRequest(
            email = "user-${UUID.randomUUID().toString().take(8)}@kanban.test",
            password = "secure-pwd-${UUID.randomUUID().toString().take(6)}",
        )

    fun refreshRequest(): AuthHandler.RefreshRequest =
        AuthHandler.RefreshRequest(
            refreshToken = "refresh-token-${UUID.randomUUID()}",
        )

    fun logoutRequest(): AuthHandler.LogoutRequest =
        AuthHandler.LogoutRequest(
            refreshToken = "refresh-token-${UUID.randomUUID()}",
        )
}
