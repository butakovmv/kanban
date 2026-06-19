package com.kanban.project

import com.kanban.common.Operation

/**
 * Операция удаления проекта по идентификатору.
 */
interface DeleteProjectOperation : Operation<DeleteProjectOperation.Arg, DeleteProjectOperation.Result> {
    /**
     * Аргумент операции удаления проекта.
     *
     * @property projectId идентификатор проекта
     */
    data class Arg(
        val projectId: String,
    )

    /**
     * Результат операции удаления проекта.
     */
    sealed interface Result {
        /** Проект успешно удалён. */
        data object Success : Result

        /** Проект не найден. */
        data object NotFound : Result
    }
}
