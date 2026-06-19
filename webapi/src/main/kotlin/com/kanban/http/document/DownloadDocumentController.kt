package com.kanban.http.document

import com.kanban.document.DocumentHandler
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Контроллер получения presigned-URL для скачивания документа.
 * Обрабатывает только запрос `GET /api/v1/documents/{id}/download`.
 *
 * @property handler обработчик запросов документов
 */
@RestController
@RequestMapping("/api/v1/documents/{id}/download")
internal class DownloadDocumentController(
    private val handler: DocumentHandler,
) {
    /**
     * Возвращает presigned-URL для скачивания содержимого документа.
     *
     * @param id идентификатор документа
     * @return 200 с URL, или 404 если документ не найден
     */
    @GetMapping
    suspend fun getDownloadUrl(
        @PathVariable("id") id: String,
    ): ResponseEntity<*> {
        val request = DocumentHandler.GetDocumentDownloadUrlRequest(documentId = id)
        val result = handler.getDownloadUrl(request)
        return when (result) {
            is DocumentHandler.GetDocumentDownloadUrlResult.Success ->
                ResponseEntity.ok(mapOf("url" to result.url))
            DocumentHandler.GetDocumentDownloadUrlResult.NotFound ->
                ResponseEntity.notFound().build<Any>()
        }
    }
}
