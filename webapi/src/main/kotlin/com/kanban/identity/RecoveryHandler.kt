package com.kanban.identity

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Обработчик запросов восстановления пароля.
 * Связывает HTTP-контроллеры с usecase-операцией [RecoveryOperation]:
 * преобразует DTO в аргументы операции и результаты обратно в DTO.
 *
 * @property recoveryOperation операция восстановления пароля
 */
internal class RecoveryHandler(
    private val recoveryOperation: RecoveryOperation,
) {
    /**
     * Запрашивает отправку токена восстановления на email.
     *
     * @param request email пользователя
     * @return результат операции
     */
    suspend fun requestRecovery(request: RecoveryRequestRequest): RecoveryResult {
        val result =
            recoveryOperation.execute(
                RecoveryOperation.Arg(
                    email = request.email,
                    action = RecoveryOperation.Action.REQUEST,
                ),
            )
        return when (result) {
            is RecoveryOperation.Result.Success -> RecoveryResult.Success(message = result.message)
            is RecoveryOperation.Result.Failure -> RecoveryResult.Failure(reason = result.reason)
        }
    }

    /**
     * Сбрасывает пароль по предъявленному токену.
     *
     * @param request token, newPassword
     * @return результат операции
     */
    suspend fun resetPassword(request: ResetPasswordRequest): RecoveryResult {
        val result =
            recoveryOperation.execute(
                RecoveryOperation.Arg(
                    email = "",
                    token = request.token,
                    newPassword = request.newPassword,
                    action = RecoveryOperation.Action.RESET,
                ),
            )
        return when (result) {
            is RecoveryOperation.Result.Success -> RecoveryResult.Success(message = result.message)
            is RecoveryOperation.Result.Failure -> RecoveryResult.Failure(reason = result.reason)
        }
    }

    /**
     * DTO запроса восстановления пароля.
     *
     * @property email email пользователя
     */
    data class RecoveryRequestRequest(
        val email: String,
    )

    /**
     * DTO запроса сброса пароля.
     *
     * @property token токен восстановления из email
     * @property newPassword новый пароль
     */
    data class ResetPasswordRequest(
        val token: String,
        @JsonProperty("new_password")
        val newPassword: String,
    )

    /**
     * Результат операции восстановления.
     */
    sealed interface RecoveryResult {
        /** Успешный результат. */
        data class Success(
            val message: String,
        ) : RecoveryResult

        /** Ошибка. */
        data class Failure(
            val reason: String,
        ) : RecoveryResult
    }
}
