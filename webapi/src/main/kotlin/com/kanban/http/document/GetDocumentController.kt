package com.kanban.http.document

import com.kanban.document.DocumentHandler
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Контроллер получения документа.
 * Обрабатывает только запрос `GET /api/v1/documents/{id}`.
 *
 * @property handler обработчик запросов документов
 */
@RestController
@RequestMapping("/api/v1/documents/{id}")
internal class GetDocumentController(
    private val handler: DocumentHandler,
) {
    /**
     * Возвращает документ по идентификатору.
     *
     * @param id идентификатор документа
     * @return 200 с документом, или 404 если документ не найден
     */
    @GetMapping
    suspend fun get(
        @PathVariable("id") id: String,
    ): ResponseEntity<*> {
        val request = DocumentHandler.GetDocumentRequest(documentId = id)
        val result = handler.get(request)
        return when (result) {
            is DocumentHandler.GetDocumentResult.Success ->
                ResponseEntity.ok(result.document)
            DocumentHandler.GetDocumentResult.NotFound ->
                ResponseEntity.notFound().build<Any>()
        }
    }
}
