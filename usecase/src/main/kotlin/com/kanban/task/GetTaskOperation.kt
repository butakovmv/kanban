package com.kanban.task

import com.kanban.common.Operation

/**
 * Операция получения задачи по идентификатору.
 */
interface GetTaskOperation : Operation<GetTaskOperation.Arg, GetTaskOperation.Result> {
    /**
     * Аргумент операции получения задачи.
     *
     * @property taskId идентификатор задачи
     */
    data class Arg(
        val taskId: String,
    )

    /**
     * Результат операции получения задачи.
     */
    sealed interface Result {
        /** Задача найдена. */
        data class Success(
            val task: Task,
        ) : Result

        /** Задача не найдена. */
        data object NotFound : Result
    }
}
