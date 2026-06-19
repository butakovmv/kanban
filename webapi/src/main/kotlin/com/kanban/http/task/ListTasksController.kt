package com.kanban.http.task

import com.kanban.task.TaskHandler
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * Контроллер получения списка задач доски.
 * Обрабатывает только запрос `GET /api/v1/boards/{boardId}/tasks`.
 *
 * @property handler обработчик запросов задач
 */
@RestController
@RequestMapping("/api/v1/boards/{boardId}/tasks")
internal class ListTasksController(
    private val handler: TaskHandler,
) {
    /**
     * Возвращает список задач доски.
     *
     * @param boardId идентификатор доски
     * @param includeArchived включать ли архивные задачи
     * @return 200 со списком задач
     */
    @GetMapping
    suspend fun list(
        @PathVariable("boardId") boardId: String,
        @RequestParam("include_archived", defaultValue = "false") includeArchived: Boolean,
    ): ResponseEntity<*> {
        val request =
            TaskHandler.ListTasksRequest(
                boardId = boardId,
                includeArchived = includeArchived,
            )
        val result = handler.list(request)
        return when (result) {
            is TaskHandler.ListTasksResult.Success ->
                ResponseEntity.ok(mapOf("tasks" to result.tasks))
        }
    }
}
