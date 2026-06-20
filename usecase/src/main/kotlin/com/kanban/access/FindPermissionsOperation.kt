package com.kanban.access

import com.kanban.common.Operation

/**
 * Операция поиска разрешений по типу ресурса и (опционально) идентификатору экземпляра.
 */
interface FindPermissionsOperation : Operation<FindPermissionsOperation.Arg, FindPermissionsOperation.Result> {
    /**
     * Аргумент операции поиска разрешений.
     *
     * @property resource тип ресурса
     * @property targetId идентификатор экземпляра ресурса или null для выборки по типу
     */
    data class Arg(
        val resource: String,
        val targetId: String?,
    )

    /**
     * Результат операции поиска разрешений.
     */
    sealed interface Result {
        /** Список разрешений успешно получен. */
        data class Success(
            val permissions: List<Permission>,
        ) : Result
    }
}
