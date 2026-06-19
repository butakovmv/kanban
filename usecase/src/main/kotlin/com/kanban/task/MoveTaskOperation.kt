package com.kanban.task

import com.kanban.common.Operation

/**
 * Операция перемещения задачи в другую колонку и/или на новую позицию.
 * При перемещении позиции остальных задач в исходной и целевой колонках пересчитываются.
 */
interface MoveTaskOperation : Operation<MoveTaskOperation.Arg, MoveTaskOperation.Result> {
    /**
     * Аргумент операции перемещения задачи.
     *
     * @property taskId идентификатор задачи
     * @property columnId идентификатор целевой колонки
     * @property position желаемая позиция задачи в целевой колонке
     */
    data class Arg(
        val taskId: String,
        val columnId: String,
        val position: Int,
    )

    /**
     * Результат операции перемещения задачи.
     */
    sealed interface Result {
        /** Задача успешно перемещена. */
        data class Success(
            val task: Task,
        ) : Result

        /** Задача не найдена. */
        data object NotFound : Result
    }
}
