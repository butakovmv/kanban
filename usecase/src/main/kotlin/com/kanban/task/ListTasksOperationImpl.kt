package com.kanban.task

/**
 * Реализация операции получения списка задач доски.
 * Делегирует запрос в репозиторий задач с учётом флага включения архивных задач.
 */
internal class ListTasksOperationImpl(
    private val taskRepository: TaskRepository,
) : ListTasksOperation {
    override suspend fun execute(arg: ListTasksOperation.Arg): ListTasksOperation.Result {
        val tasks = taskRepository.listByBoardId(arg.boardId, arg.includeArchived)
        return ListTasksOperation.Result.Success(tasks)
    }
}
