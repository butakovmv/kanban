package com.kanban.project

import com.kanban.common.Operation

/**
 * Операция создания нового проекта.
 * Создаёт проект для указанного владельца, проверяя лимиты тарифа.
 */
interface CreateProjectOperation : Operation<CreateProjectOperation.Arg, CreateProjectOperation.Result> {
    /**
     * Аргумент операции создания проекта.
     *
     * @property ownerId идентификатор пользователя-владельца
     * @property name название проекта
     * @property description описание проекта (опционально)
     */
    data class Arg(
        val ownerId: String,
        val name: String,
        val description: String?,
    )

    /**
     * Результат операции создания проекта.
     */
    sealed interface Result {
        /** Проект успешно создан. */
        data class Success(
            val project: Project,
        ) : Result

        /** Ошибка создания проекта. */
        data class Failure(
            val reason: String,
        ) : Result
    }
}
