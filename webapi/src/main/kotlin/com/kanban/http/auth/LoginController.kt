package com.kanban.http.auth

import com.kanban.identity.AuthHandler
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Контроллер входа по паролю.
 * Обрабатывает только запрос `POST /api/v1/auth/login`.
 *
 * @property handler обработчик auth-операций
 */
@RestController
@RequestMapping("/api/v1/auth/login")
internal class LoginController(
    private val handler: AuthHandler,
) {
    /**
     * Аутентифицирует пользователя по email и паролю.
     *
     * @param request данные для входа
     * @return 200 с токенами и пользователем, или 401 при ошибке
     */
    @PostMapping
    suspend fun login(
        @RequestBody request: AuthHandler.LoginRequest,
    ): ResponseEntity<*> {
        val result = handler.login(request)
        return when (result) {
            is AuthHandler.AuthResult.Success ->
                ResponseEntity.ok(result.response)
            is AuthHandler.AuthResult.Failure ->
                ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(mapOf("reason" to result.reason))
        }
    }
}
