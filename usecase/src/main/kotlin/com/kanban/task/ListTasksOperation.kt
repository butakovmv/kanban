package com.kanban.task

import com.kanban.common.Operation

/**
 * Операция получения списка задач доски.
 * По умолчанию архивные задачи исключаются из результата.
 */
interface ListTasksOperation : Operation<ListTasksOperation.Arg, ListTasksOperation.Result> {
    /**
     * Аргумент операции получения списка задач.
     *
     * @property boardId идентификатор доски
     * @property includeArchived включать ли архивные задачи
     */
    data class Arg(
        val boardId: String,
        val includeArchived: Boolean = false,
    )

    /**
     * Результат операции получения списка задач.
     */
    sealed interface Result {
        /** Список задач успешно получен. */
        data class Success(
            val tasks: List<Task>,
        ) : Result
    }
}
