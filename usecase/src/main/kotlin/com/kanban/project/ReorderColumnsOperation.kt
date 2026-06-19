package com.kanban.project

import com.kanban.common.Operation

/**
 * Операция реордеринга колонок на доске.
 * Принимает список идентификаторов колонок в новом порядке и обновляет их позиции.
 */
interface ReorderColumnsOperation : Operation<ReorderColumnsOperation.Arg, ReorderColumnsOperation.Result> {
    /**
     * Аргумент операции реордеринга колонок.
     *
     * @property boardId идентификатор доски
     * @property columnIds идентификаторы колонок в новом порядке (слева направо)
     */
    data class Arg(
        val boardId: String,
        val columnIds: List<String>,
    )

    /**
     * Результат операции реордеринга колонок.
     */
    sealed interface Result {
        /** Колонки успешно реордерены. */
        data class Success(
            val columns: List<Column>,
        ) : Result

        /** Доска не найдена. */
        data object BoardNotFound : Result

        /** Набор переданных колонок не совпадает с текущими колонками доски. */
        data object InvalidColumns : Result
    }
}
