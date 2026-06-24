package com.kanban.task

internal class ListBoardBacklogOperationImpl(
    private val taskRepository: TaskRepository,
) : ListBoardBacklogOperation {
    override suspend fun execute(arg: ListBoardBacklogOperation.Arg): ListBoardBacklogOperation.Result {
        val tasks = taskRepository.listByProjectId(arg.projectId, includeArchived = false)
        return ListBoardBacklogOperation.Result.Success(tasks)
    }
}
