package com.kanban.http.task

import com.kanban.task.TaskHandler
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Контроллер обновления задачи.
 * Обрабатывает только запрос `PUT /api/v1/tasks/{id}`.
 *
 * @property handler обработчик запросов задач
 */
@RestController
@RequestMapping("/api/v1/tasks/{id}")
internal class UpdateTaskController(
    private val handler: TaskHandler,
) {
    /**
     * Обновляет поля задачи.
     *
     * @param id идентификатор задачи
     * @param body данные для обновления
     * @return 200 с обновлённой задачей, 400 при ошибке валидации, или 404 если задача не найдена
     */
    @PutMapping
    suspend fun update(
        @PathVariable("id") id: String,
        @RequestBody body: TaskHandler.UpdateTaskBody,
    ): ResponseEntity<*> {
        val request =
            TaskHandler.UpdateTaskRequest(
                taskId = id,
                title = body.title,
                description = body.description,
                assigneeId = body.assigneeId,
                dueDate = body.dueDate,
            )
        val result = handler.update(request)
        return when (result) {
            is TaskHandler.UpdateTaskResult.Success ->
                ResponseEntity.ok(result.task)
            TaskHandler.UpdateTaskResult.NotFound ->
                ResponseEntity.notFound().build<Any>()
            is TaskHandler.UpdateTaskResult.Failure ->
                ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(mapOf("reason" to result.reason))
        }
    }
}
