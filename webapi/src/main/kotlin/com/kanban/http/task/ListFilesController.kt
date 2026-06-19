package com.kanban.http.task

import com.kanban.task.FileHandler
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Контроллер получения списка прикреплённых файлов задачи.
 * Обрабатывает только запрос `GET /api/v1/tasks/{taskId}/files`.
 *
 * @property handler обработчик запросов прикреплённых файлов
 */
@RestController
@RequestMapping("/api/v1/tasks/{taskId}/files")
internal class ListFilesController(
    private val handler: FileHandler,
) {
    /**
     * Возвращает список прикреплённых файлов задачи.
     *
     * @param taskId идентификатор задачи
     * @return 200 со списком файлов
     */
    @GetMapping
    suspend fun list(
        @PathVariable("taskId") taskId: String,
    ): ResponseEntity<*> {
        val request = FileHandler.ListFilesRequest(taskId = taskId)
        val result = handler.list(request)
        return when (result) {
            is FileHandler.ListFilesResult.Success ->
                ResponseEntity.ok(mapOf("files" to result.files))
        }
    }
}
