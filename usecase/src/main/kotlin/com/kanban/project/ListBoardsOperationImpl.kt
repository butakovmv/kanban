package com.kanban.project

internal class ListBoardsOperationImpl(
    private val boardRepository: BoardRepository,
) : ListBoardsOperation {
    override suspend fun execute(arg: ListBoardsOperation.Arg): ListBoardsOperation.Result {
        val boards = boardRepository.listByProjectId(arg.projectId)
        return ListBoardsOperation.Result.Success(boards)
    }
}
