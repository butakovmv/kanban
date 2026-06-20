package com.kanban.access

import com.kanban.common.Operation

/**
 * Операция проверки наличия у пользователя разрешения на действие над ресурсом.
 * Пользователь считается авторизованным, если он состоит хотя бы в одной группе,
 * которой назначено подходящее разрешение. Глобальные разрешения (targetId == null)
 * считаются подходящими для любого экземпляра ресурса того же типа.
 */
interface CheckPermissionOperation : Operation<CheckPermissionOperation.Arg, CheckPermissionOperation.Result> {
    /**
     * Аргумент операции проверки разрешения.
     *
     * @property userId идентификатор пользователя
     * @property resource тип ресурса
     * @property action проверяемое действие
     * @property targetId идентификатор экземпляра ресурса (null — проверка глобального разрешения)
     */
    data class Arg(
        val userId: String,
        val resource: String,
        val action: String,
        val targetId: String?,
    )

    /**
     * Результат проверки разрешения.
     */
    sealed interface Result {
        /** У пользователя есть требуемое разрешение. */
        data object Allowed : Result

        /** У пользователя нет требуемого разрешения. */
        data class Denied(
            val reason: String,
        ) : Result
    }
}
