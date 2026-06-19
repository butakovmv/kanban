package com.kanban.task

import com.kanban.common.CommentId
import com.kanban.common.TaskId
import java.time.Instant
import java.util.UUID

/**
 * Реализация операции создания комментария.
 * Проверяет существование задачи и непустой текст, создаёт комментарий и сохраняет в репозиторий.
 */
internal class CreateCommentOperationImpl(
    private val commentRepository: CommentRepository,
    private val taskRepository: TaskRepository,
) : CreateCommentOperation {
    override suspend fun execute(arg: CreateCommentOperation.Arg): CreateCommentOperation.Result {
        if (arg.text.isBlank()) {
            return CreateCommentOperation.Result.Failure("Comment text must not be blank")
        }
        if (taskRepository.findById(arg.taskId) == null) {
            return CreateCommentOperation.Result.Failure("Task not found")
        }
        val now = Instant.now()
        val comment =
            Comment(
                id = CommentId(UUID.randomUUID().toString()),
                taskId = TaskId(arg.taskId),
                authorId = arg.authorId,
                text = arg.text.trim(),
                createdAt = now,
                updatedAt = now,
            )
        val saved = commentRepository.save(comment)
        return CreateCommentOperation.Result.Success(saved)
    }
}
