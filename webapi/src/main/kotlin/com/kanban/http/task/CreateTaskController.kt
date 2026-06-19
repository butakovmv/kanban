package com.kanban.http.task

import com.kanban.task.TaskHandler
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Контроллер создания задачи.
 * Обрабатывает только запрос `POST /api/v1/tasks`.
 *
 * @property handler обработчик запросов задач
 */
@RestController
@RequestMapping("/api/v1/tasks")
internal class CreateTaskController(
    private val handler: TaskHandler,
) {
    /**
     * Создаёт новую задачу.
     *
     * @param request данные для создания задачи
     * @return 201 с созданной задачей, или 400 при ошибке
     */
    @PostMapping
    suspend fun create(
        @RequestBody request: TaskHandler.CreateTaskRequest,
    ): ResponseEntity<*> {
        val result = handler.create(request)
        return when (result) {
            is TaskHandler.CreateTaskResult.Success ->
                ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(result.task)
            is TaskHandler.CreateTaskResult.Failure ->
                ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(mapOf("reason" to result.reason))
        }
    }
}
