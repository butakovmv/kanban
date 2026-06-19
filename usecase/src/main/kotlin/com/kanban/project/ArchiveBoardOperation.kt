package com.kanban.project

import com.kanban.common.Operation

/**
 * Операция архивирования доски.
 * Архивная доска скрывается из активных списков, но сохраняется для истории.
 */
interface ArchiveBoardOperation : Operation<ArchiveBoardOperation.Arg, ArchiveBoardOperation.Result> {
    /**
     * Аргумент операции архивирования доски.
     *
     * @property boardId идентификатор доски
     */
    data class Arg(
        val boardId: String,
    )

    /**
     * Результат операции архивирования доски.
     */
    sealed interface Result {
        /** Доска успешно архивирована. */
        data object Success : Result

        /** Доска не найдена. */
        data object NotFound : Result
    }
}
