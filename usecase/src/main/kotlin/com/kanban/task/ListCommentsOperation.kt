package com.kanban.task

import com.kanban.common.Operation

/**
 * Операция получения списка комментариев задачи, упорядоченного по дате создания.
 */
interface ListCommentsOperation : Operation<ListCommentsOperation.Arg, ListCommentsOperation.Result> {
    /**
     * Аргумент операции получения списка комментариев.
     *
     * @property taskId идентификатор задачи
     */
    data class Arg(
        val taskId: String,
    )

    /**
     * Результат операции получения списка комментариев.
     */
    sealed interface Result {
        /** Список комментариев успешно получен. */
        data class Success(
            val comments: List<Comment>,
        ) : Result
    }
}
