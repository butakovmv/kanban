package com.kanban.project

import com.kanban.common.Operation

/**
 * Операция обновления полей доски.
 * Позволяет изменить название доски.
 */
interface UpdateBoardOperation : Operation<UpdateBoardOperation.Arg, UpdateBoardOperation.Result> {
    /**
     * Аргумент операции обновления доски.
     *
     * @property boardId идентификатор доски
     * @property name новое название (null — не изменять)
     */
    data class Arg(
        val boardId: String,
        val name: String?,
    )

    /**
     * Результат операции обновления доски.
     */
    sealed interface Result {
        /** Доска успешно обновлена. */
        data class Success(
            val board: Board,
        ) : Result

        /** Доска не найдена. */
        data object NotFound : Result
    }
}
