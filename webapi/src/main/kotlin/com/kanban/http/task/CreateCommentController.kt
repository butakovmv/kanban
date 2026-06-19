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

/**
 * Контроллер создания комментария.
 * Обрабатывает только запрос `POST /api/v1/tasks/{taskId}/comments`.
 *
 * @property handler обработчик запросов комментариев
 */
@RestController
@RequestMapping("/api/v1/tasks/{taskId}/comments")
internal class CreateCommentController(
    private val handler: CommentHandler,
) {
    /**
     * Создаёт комментарий к задаче.
     *
     * @param taskId идентификатор задачи
     * @param body данные для создания комментария
     * @return 201 с созданным комментарием, или 400 при ошибке
     */
    @PostMapping
    suspend fun create(
        @PathVariable("taskId") taskId: String,
        @RequestBody body: CreateCommentBody,
    ): ResponseEntity<*> {
        val request =
            CommentHandler.CreateCommentRequest(
                taskId = taskId,
                authorId = body.authorId,
                text = body.text,
            )
        val result = handler.create(request)
        return when (result) {
            is CommentHandler.CreateCommentResult.Success ->
                ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(result.comment)
            is CommentHandler.CreateCommentResult.Failure ->
                ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(mapOf("reason" to result.reason))
        }
    }

    /**
     * Тело запроса создания комментария.
     *
     * @property authorId идентификатор автора
     * @property text текст комментария
     */
    data class CreateCommentBody(
        @JsonProperty("author_id")
        val authorId: String,
        val text: String,
    )
}
