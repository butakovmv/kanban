package com.kanban.http.task

import com.kanban.task.TaskHandler
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Контроллер удаления задачи.
 * Обрабатывает только запрос `DELETE /api/v1/tasks/{id}`.
 *
 * @property handler обработчик запросов задач
 */
@RestController
@RequestMapping("/api/v1/tasks/{id}")
internal class DeleteTaskController(
    private val handler: TaskHandler,
) {
    /**
     * Удаляет задачу по идентификатору.
     *
     * @param id идентификатор задачи
     * @return 204 при успешном удалении, или 404 если задача не найдена
     */
    @DeleteMapping
    suspend fun delete(
        @PathVariable("id") id: String,
    ): ResponseEntity<*> {
        val request = TaskHandler.DeleteTaskRequest(taskId = id)
        val result = handler.delete(request)
        return when (result) {
            TaskHandler.DeleteTaskResult.Success ->
                ResponseEntity
                    .status(HttpStatus.NO_CONTENT)
                    .build<Any>()
            TaskHandler.DeleteTaskResult.NotFound ->
                ResponseEntity.notFound().build<Any>()
        }
    }
}
