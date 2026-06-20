package com.kanban.access

import com.kanban.common.Operation

/**
 * Операция получения полного списка групп пользователей.
 */
interface ListGroupsOperation : Operation<ListGroupsOperation.Arg, ListGroupsOperation.Result> {
    /**
     * Аргумент операции получения списка групп.
     * Аргумент отсутствует, так как список возвращается для всех групп.
     */
    object Arg

    /**
     * Результат операции получения списка групп.
     */
    sealed interface Result {
        /** Список групп успешно получен. */
        data class Success(
            val groups: List<Group>,
        ) : Result
    }
}
