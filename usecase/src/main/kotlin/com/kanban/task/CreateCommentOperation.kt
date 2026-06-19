package com.kanban.task

import com.kanban.common.Operation

/**
 * Операция создания комментария к задаче.
 * Проверяет существование задачи и добавляет комментарий от указанного автора.
 */
interface CreateCommentOperation : Operation<CreateCommentOperation.Arg, CreateCommentOperation.Result> {
    /**
     * Аргумент операции создания комментария.
     *
     * @property taskId идентификатор задачи
     * @property authorId идентификатор автора
     * @property text текст комментария
     */
    data class Arg(
        val taskId: String,
        val authorId: String,
        val text: String,
    )

    /**
     * Результат операции создания комментария.
     */
    sealed interface Result {
        /** Комментарий успешно создан. */
        data class Success(
            val comment: Comment,
        ) : Result

        /** Ошибка создания (задача не найдена, пустой текст и т. п.). */
        data class Failure(
            val reason: String,
        ) : Result
    }
}
