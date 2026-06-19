package com.kanban.task

import com.kanban.common.BoardId
import com.kanban.common.ColumnId
import com.kanban.common.TaskId
import com.kanban.project.BoardRepository
import com.kanban.project.ColumnRepository
import java.time.Instant
import java.util.UUID

/**
 * Реализация операции создания задачи.
 * Проверяет существование доски и колонки, валидирует заголовок, создаёт задачу
 * с позицией в конец колонки и сохраняет в репозиторий.
 */
internal class CreateTaskOperationImpl(
    private val taskRepository: TaskRepository,
    private val boardRepository: BoardRepository,
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
                boardId = BoardId(arg.boardId),
                columnId = ColumnId(arg.columnId),
                title = arg.title.trim(),
                description = arg.description,
                assigneeId = arg.assigneeId,
                position = existing.size,
                dueDate = arg.dueDate,
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
            boardRepository.findById(arg.boardId) == null ->
                CreateTaskOperation.Result.Failure("Board not found")
            columnRepository.findById(arg.columnId) == null ->
                CreateTaskOperation.Result.Failure("Column not found")
            else -> null
        }
}
