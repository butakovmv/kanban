package com.kanban.task

import com.kanban.common.ColumnId
import com.kanban.common.ProjectId
import com.kanban.common.TaskId
import com.kanban.project.ColumnRepository
import java.time.Instant
import java.util.UUID

/**
 * Реализация операции создания задачи.
 * Проверяет существование колонки, валидирует заголовок, создаёт задачу
 * с позицией в конец колонки и сохраняет в репозиторий.
 */
internal class CreateTaskOperationImpl(
    private val taskRepository: TaskRepository,
    private val columnRepository: ColumnRepository,
) : CreateTaskOperation {
    override suspend fun execute(arg: CreateTaskOperation.Arg): CreateTaskOperation.Result {
        val validation = validate(arg)
        if (validation != null) return validation

        val now = Instant.now()
        val existing = taskRepository.listByColumnId(arg.columnId)
        val task =
            Task(
                id = TaskId(UUID.randomUUID().toString()),
                projectId = ProjectId(arg.projectId),
                columnId = ColumnId(arg.columnId),
                title = arg.title.trim(),
                description = arg.description,
                assigneeId = arg.assigneeId,
                position = existing.size,
                dueDate = arg.dueDate,
                priority = arg.priority,
                archived = false,
                createdAt = now,
                updatedAt = now,
            )
        val saved = taskRepository.save(task)
        return CreateTaskOperation.Result.Success(saved)
    }

    private suspend fun validate(arg: CreateTaskOperation.Arg): CreateTaskOperation.Result.Failure? =
        when {
            arg.title.isBlank() -> CreateTaskOperation.Result.Failure("Title must not be blank")
            columnRepository.findById(arg.columnId) == null ->
                CreateTaskOperation.Result.Failure("Column not found")
            else -> null
        }
}
