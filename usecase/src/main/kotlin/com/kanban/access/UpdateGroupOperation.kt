package com.kanban.access

import com.kanban.common.Operation

/**
 * Операция обновления полей группы.
 * Позволяет изменить название и/или описание (null — не изменять соответствующее поле).
 */
interface UpdateGroupOperation : Operation<UpdateGroupOperation.Arg, UpdateGroupOperation.Result> {
    /**
     * Аргумент операции обновления группы.
     *
     * @property groupId идентификатор группы
     * @property name новое название (null — не изменять)
     * @property description новое описание (null — не изменять)
     */
    data class Arg(
        val groupId: String,
        val name: String?,
        val description: String?,
    )

    /**
     * Результат операции обновления группы.
     */
    sealed interface Result {
        /** Группа успешно обновлена. */
        data class Success(
            val group: Group,
        ) : Result

        /** Группа не найдена. */
        data object NotFound : Result

        /** Ошибка обновления (валидация входных данных). */
        data class Failure(
            val reason: String,
        ) : Result
    }
}
