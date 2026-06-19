package com.kanban.task

/**
 * Реализация операции получения задачи.
 * Делегирует поиск в репозиторий задач.
 */
internal class GetTaskOperationImpl(
    private val taskRepository: TaskRepository,
) : GetTaskOperation {
    override suspend fun execute(arg: GetTaskOperation.Arg): GetTaskOperation.Result {
        val task = taskRepository.findById(arg.taskId) ?: return GetTaskOperation.Result.NotFound
        return GetTaskOperation.Result.Success(task)
    }
}
