package com.kanban.http.auth

import com.kanban.identity.AuthHandler
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Контроллер регистрации пользователя.
 * Обрабатывает только запрос `POST /api/v1/auth/register`.
 *
 * @property handler обработчик auth-операций
 */
@RestController
@RequestMapping("/api/v1/auth/register")
internal class RegisterController(
    private val handler: AuthHandler,
) {
    /**
     * Регистрирует нового пользователя.
     *
     * @param request данные для регистрации
     * @return 201 с токенами и пользователем, или 400 при ошибке
     */
    @PostMapping
    suspend fun register(
        @RequestBody request: AuthHandler.RegisterRequest,
    ): ResponseEntity<*> {
        val result = handler.register(request)
        return when (result) {
            is AuthHandler.AuthResult.Success ->
                ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(result.response)
            is AuthHandler.AuthResult.Failure ->
                ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(mapOf("reason" to result.reason))
        }
    }
}
