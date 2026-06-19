package com.kanban.http.document

import com.kanban.document.DocumentHandler
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Контроллер удаления документа.
 * Обрабатывает только запрос `DELETE /api/v1/documents/{id}`.
 *
 * @property handler обработчик запросов документов
 */
@RestController
@RequestMapping("/api/v1/documents/{id}")
internal class DeleteDocumentController(
    private val handler: DocumentHandler,
) {
    /**
     * Удаляет документ по идентификатору.
     *
     * @param id идентификатор документа
     * @return 204 при успешном удалении, или 404 если документ не найден
     */
    @DeleteMapping
    suspend fun delete(
        @PathVariable("id") id: String,
    ): ResponseEntity<*> {
        val request = DocumentHandler.DeleteDocumentRequest(documentId = id)
        val result = handler.delete(request)
        return when (result) {
            DocumentHandler.DeleteDocumentResult.Success ->
                ResponseEntity
                    .status(HttpStatus.NO_CONTENT)
                    .build<Any>()
            DocumentHandler.DeleteDocumentResult.NotFound ->
                ResponseEntity.notFound().build<Any>()
        }
    }
}
