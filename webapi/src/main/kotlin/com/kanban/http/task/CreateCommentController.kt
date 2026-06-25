package com.kanban.http.task

import com.fasterxml.jackson.annotation.JsonProperty
import com.kanban.task.CommentHandler
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/tasks/{taskId}/comments")
internal class CreateCommentController(
    private val handler: CommentHandler,
) {
    data class CreateCommentBody(
        @JsonProperty("author_id")
        val authorId: String,
        val text: String,
    )

    @PostMapping
    suspend fun create(
        @PathVariable("taskId") taskId: String,
        @RequestBody body: CreateCommentBody,
    ): ResponseEntity<*> {
        val result =
            handler.create(
                taskId = taskId,
                authorId = body.authorId,
                text = body.text,
            )
        return when (result) {
            is CommentHandler.CreateCommentResult.Success -> {
                val comment = result.comment
                ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(
                        CommentResponse(
                            id = comment.id,
                            taskId = comment.taskId,
                            authorId = comment.authorId,
                            text = comment.text,
                            createdAt = comment.createdAt,
                            updatedAt = comment.updatedAt,
                        ),
                    )
            }
            is CommentHandler.CreateCommentResult.Failure ->
                ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(mapOf("reason" to result.reason))
        }
    }
}
