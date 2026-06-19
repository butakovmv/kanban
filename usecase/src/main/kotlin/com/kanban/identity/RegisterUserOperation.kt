package com.kanban.identity

import com.kanban.common.AuthTokens
import com.kanban.common.Operation

/**
 * Операция регистрации нового пользователя.
 * Создаёт учётную запись, хеширует пароль и возвращает токены доступа.
 */
interface RegisterUserOperation : Operation<RegisterUserOperation.Arg, RegisterUserOperation.Result> {
    /**
     * Аргумент операции регистрации.
     *
     * @property email email-адрес пользователя
     * @property password пароль пользователя
     * @property displayName отображаемое имя пользователя
     */
    data class Arg(
        val email: String,
        val password: String,
        val displayName: String,
    )

    /**
     * Результат операции регистрации.
     */
    sealed interface Result {
        /** Регистрация успешна. */
        data class Success(
            val tokens: AuthTokens,
            val user: User,
        ) : Result

        /** Ошибка регистрации. */
        data class Failure(
            val reason: String,
        ) : Result
    }
}
