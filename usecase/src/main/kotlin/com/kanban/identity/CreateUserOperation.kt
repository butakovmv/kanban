package com.kanban.identity

import com.kanban.common.Operation

/**
 * Операция создания нового пользователя в системе.
 * Используется внутренними процессами (например, при регистрации через административный интерфейс).
 */
interface CreateUserOperation : Operation<CreateUserOperation.Arg, CreateUserOperation.Result> {
    /**
     * Аргумент операции создания пользователя.
     *
     * @property email email-адрес пользователя
     * @property passwordHash хеш пароля пользователя
     * @property displayName отображаемое имя пользователя
     */
    data class Arg(
        val email: String,
        val passwordHash: String,
        val displayName: String,
    )

    /**
     * Результат операции создания пользователя.
     */
    sealed interface Result {
        /** Пользователь успешно создан. */
        data class Success(
            val user: User,
        ) : Result

        /** Ошибка создания пользователя. */
        data class Failure(
            val reason: String,
        ) : Result
    }
}
