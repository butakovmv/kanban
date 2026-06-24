package com.kanban.task

import java.time.Instant

/**
 * Реализация операции обновления задачи.
 * Находит задачу по ID, обновляет указанные поля (title, description, assigneeId, dueDate) и сохраняет.
 * Поле title обязательно непустое при обновлении.
 */
internal class UpdateTaskOperationImpl(
    private val taskRepository: TaskRepository,
) : UpdateTaskOperation {
    override suspend fun execute(arg: UpdateTaskOperation.Arg): UpdateTaskOperation.Result {
        val existing =
            taskRepository.findById(arg.taskId) ?: return UpdateTaskOperation.Result.NotFound
        if (arg.title != null && arg.title.isBlank()) {
            return UpdateTaskOperation.Result.Failure("Title must not be blank")
        }
        val updated =
            existing.copy(
                title = arg.title?.trim() ?: existing.title,
                description = arg.description ?: existing.description,
                assigneeId = arg.assigneeId ?: existing.assigneeId,
                dueDate = arg.dueDate ?: existing.dueDate,
                priority = arg.priority ?: existing.priority,
                updatedAt = Instant.now(),
            )
        val saved = taskRepository.save(updated)
        return UpdateTaskOperation.Result.Success(saved)
    }
}
