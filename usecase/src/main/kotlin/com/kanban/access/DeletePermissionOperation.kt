package com.kanban.access

import com.kanban.common.Operation

/**
 * Операция удаления разрешения по идентификатору.
 */
interface DeletePermissionOperation : Operation<DeletePermissionOperation.Arg, DeletePermissionOperation.Result> {
    /**
     * Аргумент операции удаления разрешения.
     *
     * @property permissionId идентификатор разрешения
     */
    data class Arg(
        val permissionId: String,
    )

    /**
     * Результат операции удаления разрешения.
     */
    sealed interface Result {
        /** Разрешение успешно удалено. */
        data object Success : Result

        /** Разрешение не найдено. */
        data object NotFound : Result
    }
}
