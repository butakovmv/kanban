package com.kanban.http.task

import com.kanban.task.CommentHandler
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/tasks/{taskId}/comments")
internal class ListCommentsController(
    private val handler: CommentHandler,
) {
    @GetMapping
    suspend fun list(
        @PathVariable("taskId") taskId: String,
    ): ResponseEntity<*> {
        val result = handler.list(taskId = taskId)
        return when (result) {
            is CommentHandler.ListCommentsResult.Success ->
                ResponseEntity.ok(
                    mapOf(
                        "comments" to
                            result.comments.map { comment ->
                                CommentResponse(
                                    id = comment.id,
                                    taskId = comment.taskId,
                                    authorId = comment.authorId,
                                    text = comment.text,
                                    createdAt = comment.createdAt,
                                    updatedAt = comment.updatedAt,
                                )
                            },
                    ),
                )
        }
    }
}
