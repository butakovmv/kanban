package com.kanban.project

import com.kanban.common.Operation

/**
 * Операция получения списка проектов пользователя.
 */
interface ListProjectsOperation : Operation<ListProjectsOperation.Arg, ListProjectsOperation.Result> {
    /**
     * Аргумент операции получения списка проектов.
     *
     * @property ownerId идентификатор пользователя-владельца
     */
    data class Arg(
        val ownerId: String,
    )

    /**
     * Результат операции получения списка проектов.
     */
    sealed interface Result {
        /** Список проектов успешно получен. */
        data class Success(
            val projects: List<Project>,
        ) : Result
    }
}
