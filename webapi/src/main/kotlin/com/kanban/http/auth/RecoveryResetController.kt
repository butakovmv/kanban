package com.kanban.http.auth

import com.kanban.identity.RecoveryHandler
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Контроллер сброса пароля.
 * Обрабатывает только запрос `POST /api/v1/auth/recovery/reset`.
 *
 * @property handler обработчик запросов восстановления
 */
@RestController
@RequestMapping("/api/v1/auth/recovery/reset")
internal class RecoveryResetController(
    private val handler: RecoveryHandler,
) {
    /**
     * Сбрасывает пароль пользователя по предъявленному токену.
     *
     * @param request token и newPassword
     * @return 200 с сообщением об успехе, или 400 при ошибке
     */
    @PostMapping
    suspend fun resetPassword(
        @RequestBody request: RecoveryHandler.ResetPasswordRequest,
    ): ResponseEntity<*> {
        val result = handler.resetPassword(request)
        return when (result) {
            is RecoveryHandler.RecoveryResult.Success ->
                ResponseEntity.ok(mapOf("message" to result.message))
            is RecoveryHandler.RecoveryResult.Failure ->
                ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(mapOf("reason" to result.reason))
        }
    }
}
