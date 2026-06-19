package com.kanban.http.task

import com.kanban.task.CommentHandler
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Контроллер получения списка комментариев задачи.
 * Обрабатывает только запрос `GET /api/v1/tasks/{taskId}/comments`.
 *
 * @property handler обработчик запросов комментариев
 */
@RestController
@RequestMapping("/api/v1/tasks/{taskId}/comments")
internal class ListCommentsController(
    private val handler: CommentHandler,
) {
    /**
     * Возвращает список комментариев задачи.
     *
     * @param taskId идентификатор задачи
     * @return 200 со списком комментариев
     */
    @GetMapping
    suspend fun list(
        @PathVariable("taskId") taskId: String,
    ): ResponseEntity<*> {
        val request = CommentHandler.ListCommentsRequest(taskId = taskId)
        val result = handler.list(request)
        return when (result) {
            is CommentHandler.ListCommentsResult.Success ->
                ResponseEntity.ok(mapOf("comments" to result.comments))
        }
    }
}
