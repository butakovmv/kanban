package com.kanban.access

import com.kanban.common.Operation

/**
 * Операция создания новой группы пользователей.
 * Группа используется для объединения пользователей и назначения им набора разрешений.
 */
interface CreateGroupOperation : Operation<CreateGroupOperation.Arg, CreateGroupOperation.Result> {
    /**
     * Аргумент операции создания группы.
     *
     * @property name название группы
     * @property description описание группы (опционально)
     */
    data class Arg(
        val name: String,
        val description: String?,
    )

    /**
     * Результат операции создания группы.
     */
    sealed interface Result {
        /** Группа успешно создана. */
        data class Success(
            val group: Group,
        ) : Result

        /** Ошибка создания группы (валидация и т. п.). */
        data class Failure(
            val reason: String,
        ) : Result
    }
}
