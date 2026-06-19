package com.kanban.project

/**
 * Реализация операции обновления доски.
 * Находит доску по ID, обновляет указанные поля и сохраняет.
 */
internal class UpdateBoardOperationImpl(
    private val boardRepository: BoardRepository,
) : UpdateBoardOperation {
    override suspend fun execute(arg: UpdateBoardOperation.Arg): UpdateBoardOperation.Result {
        val existing = boardRepository.findById(arg.boardId) ?: return UpdateBoardOperation.Result.NotFound
        val updated = existing.copy(name = arg.name ?: existing.name)
        val saved = boardRepository.save(updated)
        return UpdateBoardOperation.Result.Success(saved)
    }
}
