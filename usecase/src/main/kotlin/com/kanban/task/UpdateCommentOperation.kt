package com.kanban.task

import com.kanban.common.Operation

/**
 * Операция обновления текста комментария.
 */
interface UpdateCommentOperation : Operation<UpdateCommentOperation.Arg, UpdateCommentOperation.Result> {
    /**
     * Аргумент операции обновления комментария.
     *
     * @property commentId идентификатор комментария
     * @property text новый текст комментария
     */
    data class Arg(
        val commentId: String,
        val text: String,
    )

    /**
     * Результат операции обновления комментария.
     */
    sealed interface Result {
        /** Комментарий успешно обновлён. */
        data class Success(
            val comment: Comment,
        ) : Result

        /** Комментарий не найден. */
        data object NotFound : Result

        /** Ошибка обновления (валидация входных данных). */
        data class Failure(
            val reason: String,
        ) : Result
    }
}
