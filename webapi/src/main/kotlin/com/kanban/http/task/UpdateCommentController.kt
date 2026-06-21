package com.kanban.http.task

import com.kanban.task.CommentHandler
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/comments/{id}")
internal class UpdateCommentController(
    private val handler: CommentHandler,
) {
    data class UpdateCommentBody(
        val text: String,
    )

    @PutMapping
    suspend fun update(
        @PathVariable("id") id: String,
        @RequestBody body: UpdateCommentBody,
    ): ResponseEntity<*> {
        val result = handler.update(
            commentId = id,
            text = body.text,
        )
        return when (result) {
            is CommentHandler.UpdateCommentResult.Success -> {
                val comment = result.comment
                ResponseEntity.ok(
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
            CommentHandler.UpdateCommentResult.NotFound ->
                ResponseEntity.notFound().build<Any>()
            is CommentHandler.UpdateCommentResult.Failure ->
                ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(mapOf("reason" to result.reason))
        }
    }
}
