package com.kanban.access

import com.kanban.common.Operation

/**
 * Операция добавления пользователя в группу.
 */
interface AddMemberOperation : Operation<AddMemberOperation.Arg, AddMemberOperation.Result> {
    /**
     * Аргумент операции добавления члена группы.
     *
     * @property groupId идентификатор группы
     * @property userId идентификатор пользователя
     */
    data class Arg(
        val groupId: String,
        val userId: String,
    )

    /**
     * Результат операции добавления члена группы.
     */
    sealed interface Result {
        /** Пользователь успешно добавлен в группу. */
        data object Success : Result

        /** Ошибка добавления (группа не найдена, пользователь уже состоит в группе). */
        data class Failure(
            val reason: String,
        ) : Result
    }
}
