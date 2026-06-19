package com.kanban.task

import com.kanban.common.Operation

/**
 * Операция удаления задачи по идентификатору.
 */
interface DeleteTaskOperation : Operation<DeleteTaskOperation.Arg, DeleteTaskOperation.Result> {
    /**
     * Аргумент операции удаления задачи.
     *
     * @property taskId идентификатор задачи
     */
    data class Arg(
        val taskId: String,
    )

    /**
     * Результат операции удаления задачи.
     */
    sealed interface Result {
        /** Задача успешно удалена. */
        data object Success : Result

        /** Задача не найдена. */
        data object NotFound : Result
    }
}
