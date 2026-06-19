package com.kanban.task

/**
 * Реализация операции удаления задачи.
 * Находит задачу по ID, при отсутствии возвращает NotFound, иначе удаляет.
 */
internal class DeleteTaskOperationImpl(
    private val taskRepository: TaskRepository,
) : DeleteTaskOperation {
    override suspend fun execute(arg: DeleteTaskOperation.Arg): DeleteTaskOperation.Result {
        val existing = taskRepository.findById(arg.taskId) ?: return DeleteTaskOperation.Result.NotFound
        taskRepository.delete(existing.id.value)
        return DeleteTaskOperation.Result.Success
    }
}
