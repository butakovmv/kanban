package com.kanban.identity

import com.kanban.common.Operation

/**
 * Операция обновления данных пользователя.
 * Позволяет изменить отображаемое имя и/или email-адрес.
 */
interface UpdateUserOperation : Operation<UpdateUserOperation.Arg, UpdateUserOperation.Result> {
    /**
     * Аргумент операции обновления пользователя.
     *
     * @property userId идентификатор пользователя
     * @property displayName новое отображаемое имя (null — не изменять)
     * @property email новый email-адрес (null — не изменять)
     */
    data class Arg(
        val userId: String,
        val displayName: String?,
        val email: String?,
    )

    /**
     * Результат операции обновления пользователя.
     */
    sealed interface Result {
        /** Пользователь успешно обновлён. */
        data class Success(
            val user: User,
        ) : Result

        /** Пользователь не найден. */
        data object NotFound : Result

        /** Ошибка обновления. */
        data class Failure(
            val reason: String,
        ) : Result
    }
}
