package com.kanban.task

import com.kanban.common.Operation
import java.time.Instant

/**
 * Операция создания новой задачи в колонке проекта.
 * Проверяет существование проекта и колонки, создаёт задачу с позицией в конец колонки.
 */
interface CreateTaskOperation : Operation<CreateTaskOperation.Arg, CreateTaskOperation.Result> {
    /**
     * Аргумент операции создания задачи.
     *
     * @property projectId идентификатор проекта
     * @property columnId идентификатор колонки
     * @property title заголовок задачи
     * @property description описание задачи (опционально)
     * @property assigneeId идентификатор исполнителя (опционально)
     * @property dueDate срок выполнения (опционально)
     */
    data class Arg(
        val projectId: String,
        val columnId: String,
        val title: String,
        val description: String?,
        val assigneeId: String?,
        val dueDate: Instant?,
        val priority: String?,
    )

    /**
     * Результат операции создания задачи.
     */
    sealed interface Result {
        /** Задача успешно создана. */
        data class Success(
            val task: Task,
        ) : Result

        /** Ошибка создания задачи (валидация, доска/колонка не найдена и т. п.). */
        data class Failure(
            val reason: String,
        ) : Result
    }
}
