package com.kanban.identity

import com.kanban.common.Operation

/**
 * Операция восстановления пароля.
 * Позволяет пользователю запросить токен сброса пароля по email
 * и сбросить пароль, используя полученный токен.
 */
interface RecoveryOperation : Operation<RecoveryOperation.Arg, RecoveryOperation.Result> {
    /**
     * Аргумент операции восстановления.
     *
     * @property email email пользователя, запросившего восстановление
     * @property token токен восстановления (только для reset)
     * @property newPassword новый пароль (только для reset)
     * @property action действие: request или reset
     */
    data class Arg(
        val email: String,
        val token: String = "",
        val newPassword: String = "",
        val action: Action,
    )

    /**
     * Действие, которое нужно выполнить.
     */
    enum class Action {
        /** Запросить токен восстановления. */
        REQUEST,

        /** Сбросить пароль по токену. */
        RESET,
    }

    /**
     * Результат операции восстановления.
     */
    sealed interface Result {
        /** Операция успешна. */
        data class Success(
            val message: String,
        ) : Result

        /** Ошибка операции. */
        data class Failure(
            val reason: String,
        ) : Result
    }
}
