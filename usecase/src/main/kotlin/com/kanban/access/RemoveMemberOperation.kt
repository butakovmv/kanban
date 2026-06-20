package com.kanban.access

import com.kanban.common.Operation

/**
 * Операция удаления пользователя из группы.
 */
interface RemoveMemberOperation : Operation<RemoveMemberOperation.Arg, RemoveMemberOperation.Result> {
    /**
     * Аргумент операции удаления члена группы.
     *
     * @property groupId идентификатор группы
     * @property userId идентификатор пользователя
     */
    data class Arg(
        val groupId: String,
        val userId: String,
    )

    /**
     * Результат операции удаления члена группы.
     */
    sealed interface Result {
        /** Пользователь успешно удалён из группы. */
        data object Success : Result

        /** Ошибка удаления (группа не найдена, пользователь не состоит в группе). */
        data class Failure(
            val reason: String,
        ) : Result
    }
}
