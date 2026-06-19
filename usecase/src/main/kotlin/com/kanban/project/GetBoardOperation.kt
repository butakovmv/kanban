package com.kanban.project

import com.kanban.common.Operation

/**
 * Операция получения доски вместе с её колонками.
 * Возвращает DTO [BoardView].
 */
interface GetBoardOperation : Operation<GetBoardOperation.Arg, GetBoardOperation.Result> {
    /**
     * Аргумент операции получения доски.
     *
     * @property boardId идентификатор доски
     */
    data class Arg(
        val boardId: String,
    )

    /**
     * Результат операции получения доски.
     */
    sealed interface Result {
        /** Доска найдена. */
        data class Success(
            val view: BoardView,
        ) : Result

        /** Доска не найдена. */
        data object NotFound : Result
    }
}
