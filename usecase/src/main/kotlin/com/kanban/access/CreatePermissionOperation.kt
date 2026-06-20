package com.kanban.access

import com.kanban.common.Operation

/**
 * Операция создания нового разрешения на ресурс.
 * Разрешение описывает допустимое действие над ресурсом или конкретным его экземпляром.
 */
interface CreatePermissionOperation : Operation<CreatePermissionOperation.Arg, CreatePermissionOperation.Result> {
    /**
     * Аргумент операции создания разрешения.
     *
     * @property resource тип ресурса (например, "project", "board", "task")
     * @property action действие над ресурсом (например, "read", "write", "delete", "admin")
     * @property targetId идентификатор конкретного экземпляра ресурса (null — глобальное разрешение)
     */
    data class Arg(
        val resource: String,
        val action: String,
        val targetId: String?,
    )

    /**
     * Результат операции создания разрешения.
     */
    sealed interface Result {
        /** Разрешение успешно создано. */
        data class Success(
            val permission: Permission,
        ) : Result

        /** Ошибка создания разрешения (валидация и т. п.). */
        data class Failure(
            val reason: String,
        ) : Result
    }
}
