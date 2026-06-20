package com.kanban.access

import com.kanban.common.Operation

/**
 * Операция получения списка членов указанной группы.
 */
interface ListMembersOperation : Operation<ListMembersOperation.Arg, ListMembersOperation.Result> {
    /**
     * Аргумент операции получения списка членов группы.
     *
     * @property groupId идентификатор группы
     */
    data class Arg(
        val groupId: String,
    )

    /**
     * Результат операции получения списка членов группы.
     */
    sealed interface Result {
        /** Список членов группы успешно получен. */
        data class Success(
            val members: List<GroupMember>,
        ) : Result
    }
}
