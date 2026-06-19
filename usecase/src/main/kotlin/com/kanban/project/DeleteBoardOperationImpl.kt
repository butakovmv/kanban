package com.kanban.project

/**
 * Реализация операции удаления доски.
 * Находит доску по ID, при отсутствии возвращает NotFound, иначе удаляет.
 */
internal class DeleteBoardOperationImpl(
    private val boardRepository: BoardRepository,
) : DeleteBoardOperation {
    override suspend fun execute(arg: DeleteBoardOperation.Arg): DeleteBoardOperation.Result {
        val existing = boardRepository.findById(arg.boardId) ?: return DeleteBoardOperation.Result.NotFound
        boardRepository.delete(existing.id.value)
        return DeleteBoardOperation.Result.Success
    }
}
