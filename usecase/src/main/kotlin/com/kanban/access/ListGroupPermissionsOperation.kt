package com.kanban.access

import com.kanban.common.Operation

/**
 * Операция получения списка разрешений, назначенных указанной группе.
 */
@Suppress("MaxLineLength")
interface ListGroupPermissionsOperation : Operation<ListGroupPermissionsOperation.Arg, ListGroupPermissionsOperation.Result> {
    /**
     * Аргумент операции получения списка разрешений группы.
     *
     * @property groupId идентификатор группы
     */
    data class Arg(
        val groupId: String,
    )

    /**
     * Результат операции получения списка разрешений группы.
     */
    sealed interface Result {
        /** Список разрешений группы успешно получен. */
        data class Success(
            val permissions: List<Permission>,
        ) : Result
    }
}
