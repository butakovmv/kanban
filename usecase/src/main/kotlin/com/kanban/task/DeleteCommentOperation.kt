package com.kanban.task

import com.kanban.common.Operation

/**
 * Операция удаления комментария по идентификатору.
 */
interface DeleteCommentOperation : Operation<DeleteCommentOperation.Arg, DeleteCommentOperation.Result> {
    /**
     * Аргумент операции удаления комментария.
     *
     * @property commentId идентификатор комментария
     */
    data class Arg(
        val commentId: String,
    )

    /**
     * Результат операции удаления комментария.
     */
    sealed interface Result {
        /** Комментарий успешно удалён. */
        data object Success : Result

        /** Комментарий не найден. */
        data object NotFound : Result
    }
}
