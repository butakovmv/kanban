package com.kanban.http.task

import java.time.Instant
import java.util.UUID

internal object RequestGenerator {
    fun createTaskRequest(): CreateTaskController.CreateTaskBody =
        CreateTaskController.CreateTaskBody(
            boardId = "board-${UUID.randomUUID()}",
            columnId = "column-${UUID.randomUUID()}",
            title = "Task ${UUID.randomUUID().toString().take(6)}",
            description = "Description ${UUID.randomUUID().toString().take(6)}",
            assigneeId = "user-${UUID.randomUUID()}",
            dueDate = Instant.now().plusSeconds(86_400),
        )

    fun createTaskRequestWithoutOptionals(): CreateTaskController.CreateTaskBody =
        CreateTaskController.CreateTaskBody(
            boardId = "board-${UUID.randomUUID()}",
            columnId = "column-${UUID.randomUUID()}",
            title = "Task ${UUID.randomUUID().toString().take(6)}",
            description = null,
            assigneeId = null,
            dueDate = null,
        )

    fun updateTaskBody(): UpdateTaskController.UpdateTaskBody =
        UpdateTaskController.UpdateTaskBody(
            title = "Updated ${UUID.randomUUID().toString().take(6)}",
            description = "Updated ${UUID.randomUUID().toString().take(6)}",
            assigneeId = "user-${UUID.randomUUID()}",
            dueDate = Instant.now().plusSeconds(172_800),
        )

    fun moveTaskBody(): MoveTaskController.MoveTaskBody =
        MoveTaskController.MoveTaskBody(
            columnId = "column-${UUID.randomUUID()}",
            position = (0..100).random(),
        )

    fun createCommentBody(): CreateCommentController.CreateCommentBody =
        CreateCommentController.CreateCommentBody(
            authorId = "user-${UUID.randomUUID()}",
            text = "Comment ${UUID.randomUUID().toString().take(6)}",
        )

    fun updateCommentBody(): UpdateCommentController.UpdateCommentBody =
        UpdateCommentController.UpdateCommentBody(
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
