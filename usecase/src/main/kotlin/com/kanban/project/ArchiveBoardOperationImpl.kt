package com.kanban.project

/**
 * Реализация операции архивирования доски.
 * Находит доску по ID, при отсутствии возвращает NotFound, иначе архивирует.
 */
internal class ArchiveBoardOperationImpl(
    private val boardRepository: BoardRepository,
) : ArchiveBoardOperation {
    override suspend fun execute(arg: ArchiveBoardOperation.Arg): ArchiveBoardOperation.Result {
        val existing = boardRepository.findById(arg.boardId) ?: return ArchiveBoardOperation.Result.NotFound
        boardRepository.archive(existing.id.value)
        return ArchiveBoardOperation.Result.Success
    }
}
