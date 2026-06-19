package com.kanban.http.task

import com.kanban.task.FileHandler
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Контроллер получения URL для скачивания прикреплённого файла.
 * Обрабатывает только запрос `GET /api/v1/files/{id}/download`.
 *
 * @property handler обработчик запросов прикреплённых файлов
 */
@RestController
@RequestMapping("/api/v1/files/{id}/download")
internal class GetFileDownloadUrlController(
    private val handler: FileHandler,
) {
    /**
     * Возвращает presigned-URL для скачивания файла.
     *
     * @param id идентификатор прикрепления
     * @return 200 с URL, или 404 если файл не найден
     */
    @GetMapping
    suspend fun getDownloadUrl(
        @PathVariable("id") id: String,
    ): ResponseEntity<*> {
        val request = FileHandler.GetFileDownloadUrlRequest(fileId = id)
        val result = handler.getDownloadUrl(request)
        return when (result) {
            is FileHandler.GetFileDownloadUrlResult.Success ->
                ResponseEntity.ok(mapOf("url" to result.url))
            FileHandler.GetFileDownloadUrlResult.NotFound ->
                ResponseEntity.notFound().build<Any>()
        }
    }
}
