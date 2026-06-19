package com.kanban.http.task

import com.kanban.task.CommentHandler
import com.kanban.task.TaskHandler
import java.time.Instant
import java.util.UUID

/**
 * Генератор тестовых DTO для запросов задач, комментариев и файлов.
 * Создаёт случайные данные, которые используются в тестах контроллеров.
 */
internal object RequestGenerator {
    fun createTaskRequest(): TaskHandler.CreateTaskRequest =
        TaskHandler.CreateTaskRequest(
            boardId = "board-${UUID.randomUUID()}",
            columnId = "column-${UUID.randomUUID()}",
            title = "Task ${UUID.randomUUID().toString().take(6)}",
            description = "Description ${UUID.randomUUID().toString().take(6)}",
            assigneeId = "user-${UUID.randomUUID()}",
            dueDate = Instant.now().plusSeconds(86_400),
        )

    fun createTaskRequestWithoutOptionals(): TaskHandler.CreateTaskRequest =
        TaskHandler.CreateTaskRequest(
            boardId = "board-${UUID.randomUUID()}",
            columnId = "column-${UUID.randomUUID()}",
            title = "Task ${UUID.randomUUID().toString().take(6)}",
            description = null,
            assigneeId = null,
            dueDate = null,
        )

    fun updateTaskBody(): TaskHandler.UpdateTaskBody =
        TaskHandler.UpdateTaskBody(
            title = "Updated ${UUID.randomUUID().toString().take(6)}",
            description = "Updated ${UUID.randomUUID().toString().take(6)}",
            assigneeId = "user-${UUID.randomUUID()}",
            dueDate = Instant.now().plusSeconds(172_800),
        )

    fun moveTaskBody(): TaskHandler.MoveTaskBody =
        TaskHandler.MoveTaskBody(
            columnId = "column-${UUID.randomUUID()}",
            position = (0..100).random(),
        )

    fun createCommentBody(): CreateCommentController.CreateCommentBody =
        CreateCommentController.CreateCommentBody(
            authorId = "user-${UUID.randomUUID()}",
            text = "Comment ${UUID.randomUUID().toString().take(6)}",
        )

    fun updateCommentBody(): CommentHandler.UpdateCommentBody =
        CommentHandler.UpdateCommentBody(
            text = "Updated ${UUID.randomUUID().toString().take(6)}",
        )

    fun attachFileBody(): AttachFileController.AttachFileBody =
        AttachFileController.AttachFileBody(
            fileName = "file-${UUID.randomUUID().toString().take(6)}.pdf",
            contentType = "application/pdf",
            contentBase64 = "dGVzdA==",
            uploadedBy = "user-${UUID.randomUUID()}",
        )
}
