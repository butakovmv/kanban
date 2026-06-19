package com.kanban.http.task

import com.kanban.task.CommentHandler
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Контроллер удаления комментария.
 * Обрабатывает только запрос `DELETE /api/v1/comments/{id}`.
 *
 * @property handler обработчик запросов комментариев
 */
@RestController
@RequestMapping("/api/v1/comments/{id}")
internal class DeleteCommentController(
    private val handler: CommentHandler,
) {
    /**
     * Удаляет комментарий по идентификатору.
     *
     * @param id идентификатор комментария
     * @return 204 при успешном удалении, или 404 если комментарий не найден
     */
    @DeleteMapping
    suspend fun delete(
        @PathVariable("id") id: String,
    ): ResponseEntity<*> {
        val request = CommentHandler.DeleteCommentRequest(commentId = id)
        val result = handler.delete(request)
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
