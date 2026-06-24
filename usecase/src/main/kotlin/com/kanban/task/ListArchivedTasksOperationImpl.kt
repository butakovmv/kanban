package com.kanban.task

internal class ListArchivedTasksOperationImpl(
    private val taskRepository: TaskRepository,
) : ListArchivedTasksOperation {
    override suspend fun execute(arg: ListArchivedTasksOperation.Arg): ListArchivedTasksOperation.Result {
        val tasks = taskRepository.listByProjectId(arg.projectId, includeArchived = true).filter { it.archived }
        return ListArchivedTasksOperation.Result.Success(tasks)
    }
}
