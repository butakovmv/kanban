package com.kanban.identity

import com.kanban.common.Operation

/**
 * Операция выхода из системы.
 * Аннулирует refresh-токен пользователя, предотвращая дальнейшее обновление access-токена.
 */
interface LogoutOperation : Operation<LogoutOperation.Arg, LogoutOperation.Result> {
    /**
     * Аргумент операции выхода.
     *
     * @property refreshToken refresh-токен для аннулирования
     */
    data class Arg(
        val refreshToken: String,
    )

    /**
     * Результат операции выхода.
     */
    sealed interface Result {
        /** Выход выполнен. */
        data object Success : Result

        /** Ошибка выхода. */
        data class Failure(
            val reason: String,
        ) : Result
    }
}
