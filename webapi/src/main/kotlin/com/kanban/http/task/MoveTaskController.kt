package com.kanban.http.task

import com.kanban.task.TaskHandler
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Контроллер перемещения задачи.
 * Обрабатывает только запрос `PATCH /api/v1/tasks/{id}/move`.
 *
 * @property handler обработчик запросов задач
 */
@RestController
@RequestMapping("/api/v1/tasks/{id}/move")
internal class MoveTaskController(
    private val handler: TaskHandler,
) {
    /**
     * Перемещает задачу в другую колонку и/или на новую позицию.
     *
     * @param id идентификатор задачи
     * @param body данные о целевой колонке и позиции
     * @return 200 с перемещённой задачей, или 404 если задача не найдена
     */
    @PatchMapping
    suspend fun move(
        @PathVariable("id") id: String,
        @RequestBody body: TaskHandler.MoveTaskBody,
    ): ResponseEntity<*> {
        val request =
            TaskHandler.MoveTaskRequest(
                taskId = id,
                columnId = body.columnId,
                position = body.position,
            )
        val result = handler.move(request)
        return when (result) {
            is TaskHandler.MoveTaskResult.Success ->
                ResponseEntity.ok(result.task)
            TaskHandler.MoveTaskResult.NotFound ->
                ResponseEntity.notFound().build<Any>()
        }
    }
}
