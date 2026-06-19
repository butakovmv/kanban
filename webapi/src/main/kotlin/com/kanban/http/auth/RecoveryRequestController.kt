package com.kanban.http.auth

import com.kanban.identity.RecoveryHandler
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Контроллер запроса восстановления пароля.
 * Обрабатывает только запрос `POST /api/v1/auth/recovery/request`.
 *
 * @property handler обработчик запросов восстановления
 */
@RestController
@RequestMapping("/api/v1/auth/recovery/request")
internal class RecoveryRequestController(
    private val handler: RecoveryHandler,
) {
    /**
     * Запрашивает отправку токена восстановления на email.
     *
     * @param request email пользователя
     * @return 200 с сообщением об успешной отправке, или 400 при ошибке
     */
    @PostMapping
    suspend fun requestRecovery(
        @RequestBody request: RecoveryHandler.RecoveryRequestRequest,
    ): ResponseEntity<*> {
        val result = handler.requestRecovery(request)
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
