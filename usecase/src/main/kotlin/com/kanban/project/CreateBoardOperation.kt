package com.kanban.project

import com.kanban.common.Operation

/**
 * Операция создания новой доски в проекте.
 * Проверяет лимиты тарифа, создаёт доску с набором колонок по умолчанию.
 */
interface CreateBoardOperation : Operation<CreateBoardOperation.Arg, CreateBoardOperation.Result> {
    /**
     * Аргумент операции создания доски.
     *
     * @property projectId идентификатор проекта
     * @property name название доски
     */
    data class Arg(
        val projectId: String,
        val name: String,
    )

    /**
     * Результат операции создания доски.
     */
    sealed interface Result {
        /** Доска успешно создана. */
        data class Success(
            val view: BoardView,
        ) : Result

        /** Ошибка создания доски. */
        data class Failure(
            val reason: String,
        ) : Result
    }
}
