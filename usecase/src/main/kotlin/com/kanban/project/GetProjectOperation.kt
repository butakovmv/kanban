package com.kanban.project

import com.kanban.common.Operation

/**
 * Операция получения проекта по идентификатору.
 */
interface GetProjectOperation : Operation<GetProjectOperation.Arg, GetProjectOperation.Result> {
    /**
     * Аргумент операции получения проекта.
     *
     * @property projectId идентификатор проекта
     */
    data class Arg(
        val projectId: String,
    )

    /**
     * Результат операции получения проекта.
     */
    sealed interface Result {
        /** Проект найден. */
        data class Success(
            val project: Project,
        ) : Result

        /** Проект не найден. */
        data object NotFound : Result
    }
}
