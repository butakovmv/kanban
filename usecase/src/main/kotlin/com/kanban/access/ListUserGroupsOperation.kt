package com.kanban.access

import com.kanban.common.Operation

/**
 * Операция получения списка групп, в которые входит указанный пользователь.
 */
interface ListUserGroupsOperation : Operation<ListUserGroupsOperation.Arg, ListUserGroupsOperation.Result> {
    /**
     * Аргумент операции получения списка групп пользователя.
     *
     * @property userId идентификатор пользователя
     */
    data class Arg(
        val userId: String,
    )

    /**
     * Результат операции получения списка групп пользователя.
     */
    sealed interface Result {
        /** Список групп пользователя успешно получен. */
        data class Success(
            val groups: List<Group>,
        ) : Result
    }
}
