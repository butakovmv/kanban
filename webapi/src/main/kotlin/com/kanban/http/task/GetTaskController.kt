package com.kanban.http.task

import com.kanban.task.TaskHandler
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Контроллер получения задачи.
 * Обрабатывает только запрос `GET /api/v1/tasks/{id}`.
 *
 * @property handler обработчик запросов задач
 */
@RestController
@RequestMapping("/api/v1/tasks/{id}")
internal class GetTaskController(
    private val handler: TaskHandler,
) {
    /**
     * Возвращает задачу по идентификатору.
     *
     * @param id идентификатор задачи
     * @return 200 с задачей, или 404 если задача не найдена
     */
    @GetMapping
    suspend fun get(
        @PathVariable("id") id: String,
    ): ResponseEntity<*> {
        val request = TaskHandler.GetTaskRequest(taskId = id)
        val result = handler.get(request)
        return when (result) {
            is TaskHandler.GetTaskResult.Success ->
                ResponseEntity.ok(result.task)
            TaskHandler.GetTaskResult.NotFound ->
                ResponseEntity.notFound().build<Any>()
        }
    }
}
