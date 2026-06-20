package com.kanban.access

import com.kanban.common.Operation

/**
 * Операция отзыва разрешения у группы.
 */
interface RevokePermissionOperation : Operation<RevokePermissionOperation.Arg, RevokePermissionOperation.Result> {
    /**
     * Аргумент операции отзыва разрешения.
     *
     * @property groupId идентификатор группы
     * @property permissionId идентификатор разрешения
     */
    data class Arg(
        val groupId: String,
        val permissionId: String,
    )

    /**
     * Результат операции отзыва разрешения.
     */
    sealed interface Result {
        /** Разрешение успешно отозвано у группы. */
        data object Success : Result

        /** Ошибка отзыва (группа или разрешение не найдены). */
        data class Failure(
            val reason: String,
        ) : Result
    }
}
