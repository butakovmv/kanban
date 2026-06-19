package com.kanban.project

import com.kanban.common.BoardId

/**
 * Реализация операции реордеринга колонок доски.
 * Проверяет, что доска существует и набор переданных идентификаторов колонок совпадает
 * с текущими колонками доски, после чего обновляет позиции колонок в новом порядке.
 */
internal class ReorderColumnsOperationImpl(
    private val boardRepository: BoardRepository,
    private val columnRepository: ColumnRepository,
) : ReorderColumnsOperation {
    override suspend fun execute(arg: ReorderColumnsOperation.Arg): ReorderColumnsOperation.Result {
        val board =
            boardRepository.findById(arg.boardId) ?: return ReorderColumnsOperation.Result.BoardNotFound

        val currentColumns = columnRepository.listByBoardId(arg.boardId)
        val validation = validateColumns(currentColumns, arg.columnIds)
        if (validation != null) return validation

        val reordered = reorder(currentColumns, arg.columnIds, board.id)
        columnRepository.updatePositions(reordered)
        return ReorderColumnsOperation.Result.Success(reordered)
    }

    private fun validateColumns(
        current: List<Column>,
        requested: List<String>,
    ): ReorderColumnsOperation.Result.InvalidColumns? {
        val currentIds = current.map { it.id.value }.toSet()
        val requestedIds = requested.toSet()
        if (currentIds != requestedIds) return ReorderColumnsOperation.Result.InvalidColumns
        if (current.any { it.id.value !in requestedIds }) return ReorderColumnsOperation.Result.InvalidColumns
        return null
    }

    private fun reorder(
        current: List<Column>,
        requestedIds: List<String>,
        boardId: BoardId,
    ): List<Column> {
        val byId = current.associateBy { it.id.value }
        return requestedIds.mapIndexed { index, id ->
            byId.getValue(id).copy(boardId = boardId, position = index)
        }
    }
}
