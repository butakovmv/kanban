package com.kanban.task

/**
 * Реализация операции архивирования задачи.
 * Находит задачу по ID, при отсутствии возвращает NotFound, иначе архивирует.
 */
internal class ArchiveTaskOperationImpl(
    private val taskRepository: TaskRepository,
) : ArchiveTaskOperation {
    override suspend fun execute(arg: ArchiveTaskOperation.Arg): ArchiveTaskOperation.Result {
        val existing = taskRepository.findById(arg.taskId) ?: return ArchiveTaskOperation.Result.NotFound
        taskRepository.archive(existing.id.value)
        return ArchiveTaskOperation.Result.Success
    }
}
