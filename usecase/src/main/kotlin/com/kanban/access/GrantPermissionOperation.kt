package com.kanban.access

import com.kanban.common.Operation

/**
 * Операция выдачи разрешения группе.
 */
interface GrantPermissionOperation : Operation<GrantPermissionOperation.Arg, GrantPermissionOperation.Result> {
    /**
     * Аргумент операции выдачи разрешения.
     *
     * @property groupId идентификатор группы
     * @property permissionId идентификатор разрешения
     */
    data class Arg(
        val groupId: String,
        val permissionId: String,
    )

    /**
     * Результат операции выдачи разрешения.
     */
    sealed interface Result {
        /** Разрешение успешно выдано группе. */
        data object Success : Result

        /** Ошибка выдачи (группа или разрешение не найдены). */
        data class Failure(
            val reason: String,
        ) : Result
    }
}
