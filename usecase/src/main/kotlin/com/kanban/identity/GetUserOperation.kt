package com.kanban.identity

import com.kanban.common.Operation

/**
 * Операция получения пользователя по идентификатору.
 */
interface GetUserOperation : Operation<GetUserOperation.Arg, GetUserOperation.Result> {
    /**
     * Аргумент операции получения пользователя.
     *
     * @property userId уникальный идентификатор пользователя
     */
    data class Arg(
        val userId: String,
    )

    /**
     * Результат операции получения пользователя.
     */
    sealed interface Result {
        /** Пользователь найден. */
        data class Success(
            val user: User,
        ) : Result

        /** Пользователь не найден. */
        data object NotFound : Result
    }
}
