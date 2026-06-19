package com.kanban.identity

import com.kanban.common.AuthTokens
import com.kanban.common.Operation

/**
 * Операция обновления access-токена с использованием refresh-токена.
 * Проверяет действительность refresh-токена и выдаёт новую пару токенов.
 */
interface RefreshTokenOperation : Operation<RefreshTokenOperation.Arg, RefreshTokenOperation.Result> {
    /**
     * Аргумент операции обновления токена.
     *
     * @property refreshToken refresh-токен, выданный ранее при аутентификации
     */
    data class Arg(
        val refreshToken: String,
    )

    /**
     * Результат операции обновления токена.
     */
    sealed interface Result {
        /** Обновление успешно. */
        data class Success(
            val tokens: AuthTokens,
        ) : Result

        /** Ошибка обновления. */
        data class Failure(
            val reason: String,
        ) : Result
    }
}
