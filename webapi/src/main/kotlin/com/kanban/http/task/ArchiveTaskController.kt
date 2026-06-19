package com.kanban.http.task

import com.kanban.task.TaskHandler
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Контроллер архивирования задачи.
 * Обрабатывает только запрос `POST /api/v1/tasks/{id}/archive`.
 *
 * @property handler обработчик запросов задач
 */
@RestController
@RequestMapping("/api/v1/tasks/{id}/archive")
internal class ArchiveTaskController(
    private val handler: TaskHandler,
) {
    /**
     * Архивирует задачу.
     *
     * @param id идентификатор задачи
     * @return 204 при успешном архивировании, или 404 если задача не найдена
     */
    @PostMapping
    suspend fun archive(
        @PathVariable("id") id: String,
    ): ResponseEntity<*> {
        val request = TaskHandler.ArchiveTaskRequest(taskId = id)
        val result = handler.archive(request)
        return when (result) {
            TaskHandler.ArchiveTaskResult.Success ->
                ResponseEntity
                    .status(HttpStatus.NO_CONTENT)
                    .build<Any>()
            TaskHandler.ArchiveTaskResult.NotFound ->
                ResponseEntity.notFound().build<Any>()
        }
    }
}
