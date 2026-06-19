package com.kanban.http.document

import com.kanban.document.DocumentHandler
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Контроллер обновления метаданных документа.
 * Обрабатывает только запрос `PUT /api/v1/documents/{id}`.
 *
 * @property handler обработчик запросов документов
 */
@RestController
@RequestMapping("/api/v1/documents/{id}")
internal class UpdateDocumentController(
    private val handler: DocumentHandler,
) {
    /**
     * Обновляет метаданные документа (заголовок и/или описание).
     *
     * @param id идентификатор документа
     * @param body данные для обновления
     * @return 200 с обновлённым документом, 400 при ошибке валидации, или 404 если документ не найден
     */
    @PutMapping
    suspend fun update(
        @PathVariable("id") id: String,
        @RequestBody body: DocumentHandler.UpdateDocumentBody,
    ): ResponseEntity<*> {
        val request =
            DocumentHandler.UpdateDocumentRequest(
                documentId = id,
                title = body.title,
                description = body.description,
            )
        val result = handler.update(request)
        return when (result) {
            is DocumentHandler.UpdateDocumentResult.Success ->
                ResponseEntity.ok(result.document)
            DocumentHandler.UpdateDocumentResult.NotFound ->
                ResponseEntity.notFound().build<Any>()
            is DocumentHandler.UpdateDocumentResult.Failure ->
                ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(mapOf("reason" to result.reason))
        }
    }
}
