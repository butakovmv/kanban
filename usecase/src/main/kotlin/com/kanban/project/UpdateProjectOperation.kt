package com.kanban.project

import com.kanban.common.Operation

/**
 * Операция обновления полей проекта.
 * Позволяет изменить название и/или описание (null — не изменять соответствующее поле).
 */
interface UpdateProjectOperation : Operation<UpdateProjectOperation.Arg, UpdateProjectOperation.Result> {
    /**
     * Аргумент операции обновления проекта.
     *
     * @property projectId идентификатор проекта
     * @property name новое название (null — не изменять)
     * @property description новое описание (null — не изменять)
     */
    data class Arg(
        val projectId: String,
        val name: String?,
        val description: String?,
    )

    /**
     * Результат операции обновления проекта.
     */
    sealed interface Result {
        /** Проект успешно обновлён. */
        data class Success(
            val project: Project,
        ) : Result

        /** Проект не найден. */
        data object NotFound : Result
    }
}
