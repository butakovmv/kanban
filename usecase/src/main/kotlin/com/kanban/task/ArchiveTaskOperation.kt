package com.kanban.task

import com.kanban.common.Operation

/**
 * Операция архивирования задачи.
 * Архивная задача скрывается из активных списков, но сохраняется для истории.
 */
interface ArchiveTaskOperation : Operation<ArchiveTaskOperation.Arg, ArchiveTaskOperation.Result> {
    /**
     * Аргумент операции архивирования задачи.
     *
     * @property taskId идентификатор задачи
     */
    data class Arg(
        val taskId: String,
    )

    /**
     * Результат операции архивирования задачи.
     */
    sealed interface Result {
        /** Задача успешно архивирована. */
        data object Success : Result

        /** Задача не найдена. */
        data object NotFound : Result
    }
}
