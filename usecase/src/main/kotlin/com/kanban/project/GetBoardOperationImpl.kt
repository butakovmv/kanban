package com.kanban.project

/**
 * Реализация операции получения доски.
 * Находит доску по ID, загружает её колонки (упорядоченные по позиции) и возвращает [BoardView].
 */
internal class GetBoardOperationImpl(
    private val boardRepository: BoardRepository,
    private val columnRepository: ColumnRepository,
) : GetBoardOperation {
    override suspend fun execute(arg: GetBoardOperation.Arg): GetBoardOperation.Result {
        val board = boardRepository.findById(arg.boardId) ?: return GetBoardOperation.Result.NotFound
        val columns = columnRepository.listByProjectId(board.projectId.value)
        return GetBoardOperation.Result.Success(BoardView(board = board, columns = columns))
    }
}
