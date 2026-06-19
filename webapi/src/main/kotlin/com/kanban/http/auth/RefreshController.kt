package com.kanban.http.auth

import com.kanban.identity.AuthHandler
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Контроллер обновления access-токена.
 * Обрабатывает только запрос `POST /api/v1/auth/refresh`.
 *
 * @property handler обработчик auth-операций
 */
@RestController
@RequestMapping("/api/v1/auth/refresh")
internal class RefreshController(
    private val handler: AuthHandler,
) {
    /**
     * Обновляет пару токенов по refresh-токену.
     *
     * @param request refresh-токен
     * @return 200 с новыми токенами, или 401 при недействительном токене
     */
    @PostMapping
    suspend fun refresh(
        @RequestBody request: AuthHandler.RefreshRequest,
    ): ResponseEntity<*> {
        val result = handler.refresh(request)
        return when (result) {
            is AuthHandler.TokenResult.Success ->
                ResponseEntity.ok(result.response)
            is AuthHandler.TokenResult.Failure ->
                ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(mapOf("reason" to result.reason))
        }
    }
}
