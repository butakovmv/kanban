package com.kanban.http.task

import com.kanban.task.CommentHandler
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/comments/{id}")
internal class DeleteCommentController(
    private val handler: CommentHandler,
) {
    @DeleteMapping
    suspend fun delete(
        @PathVariable("id") id: String,
    ): ResponseEntity<*> {
        val result = handler.delete(commentId = id)
        return when (result) {
            CommentHandler.DeleteCommentResult.Success ->
                ResponseEntity
                    .status(HttpStatus.NO_CONTENT)
                    .build<Any>()
            CommentHandler.DeleteCommentResult.NotFound ->
                ResponseEntity.notFound().build<Any>()
        }
    }
}
