package com.kanban.http.task

import com.kanban.task.CommentHandler
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Контроллер обновления комментария.
 * Обрабатывает только запрос `PUT /api/v1/comments/{id}`.
 *
 * @property handler обработчик запросов комментариев
 */
@RestController
@RequestMapping("/api/v1/comments/{id}")
internal class UpdateCommentController(
    private val handler: CommentHandler,
) {
    /**
     * Обновляет текст комментария.
     *
     * @param id идентификатор комментария
     * @param body новый текст
     * @return 200 с обновлённым комментарием, 400 при ошибке валидации, или 404 если комментарий не найден
     */
    @PutMapping
    suspend fun update(
        @PathVariable("id") id: String,
        @RequestBody body: CommentHandler.UpdateCommentBody,
    ): ResponseEntity<*> {
        val request =
            CommentHandler.UpdateCommentRequest(
                commentId = id,
                text = body.text,
            )
        val result = handler.update(request)
        return when (result) {
            is CommentHandler.UpdateCommentResult.Success ->
                ResponseEntity.ok(result.comment)
            CommentHandler.UpdateCommentResult.NotFound ->
                ResponseEntity.notFound().build<Any>()
            is CommentHandler.UpdateCommentResult.Failure ->
                ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(mapOf("reason" to result.reason))
        }
    }
}
