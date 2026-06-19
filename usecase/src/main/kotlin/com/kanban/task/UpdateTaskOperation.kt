package com.kanban.task

import com.kanban.common.Operation
import java.time.Instant

/**
 * Операция обновления полей задачи.
 * Позволяет изменить заголовок, описание, исполнителя и/или срок выполнения.
 * Значение null в любом из обновляемых полей означает «не изменять соответствующее поле».
 */
interface UpdateTaskOperation : Operation<UpdateTaskOperation.Arg, UpdateTaskOperation.Result> {
    /**
     * Аргумент операции обновления задачи.
     *
     * @property taskId идентификатор задачи
     * @property title новый заголовок (null — не изменять)
     * @property description новое описание (null — не изменять)
     * @property assigneeId новый идентификатор исполнителя (null — не изменять)
     * @property dueDate новый срок выполнения (null — не изменять)
     */
    data class Arg(
        val taskId: String,
        val title: String?,
        val description: String?,
        val assigneeId: String?,
        val dueDate: Instant?,
    )

    /**
     * Результат операции обновления задачи.
     */
    sealed interface Result {
        /** Задача успешно обновлена. */
        data class Success(
            val task: Task,
        ) : Result

        /** Задача не найдена. */
        data object NotFound : Result

        /** Ошибка обновления (валидация входных данных). */
        data class Failure(
            val reason: String,
        ) : Result
    }
}
