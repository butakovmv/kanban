package com.kanban.http.auth

import com.kanban.identity.AuthHandler
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

/**
 * Контроллер выхода из системы.
 * Обрабатывает только запрос `POST /api/v1/auth/logout`.
 *
 * @property handler обработчик auth-операций
 */
@RestController
@RequestMapping("/api/v1/auth/logout")
internal class LogoutController(
    private val handler: AuthHandler,
) {
    /**
     * Аннулирует refresh-токен пользователя.
     *
     * @param request refresh-токен для аннулирования
     */
    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun logout(
        @RequestBody request: AuthHandler.LogoutRequest,
    ) {
        handler.logout(request)
    }
}
