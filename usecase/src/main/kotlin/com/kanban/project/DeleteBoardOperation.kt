package com.kanban.project

import com.kanban.common.Operation

/**
 * Операция удаления доски.
 */
interface DeleteBoardOperation : Operation<DeleteBoardOperation.Arg, DeleteBoardOperation.Result> {
    /**
     * Аргумент операции удаления доски.
     *
     * @property boardId идентификатор доски
     */
    data class Arg(
        val boardId: String,
    )

    /**
     * Результат операции удаления доски.
     */
    sealed interface Result {
        /** Доска успешно удалена. */
        data object Success : Result

        /** Доска не найдена. */
        data object NotFound : Result
    }
}
