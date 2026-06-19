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
 * Контроллер замены содержимого документа.
 * Обрабатывает только запрос `PUT /api/v1/documents/{id}/content`.
 *
 * @property handler обработчик запросов документов
 */
@RestController
@RequestMapping("/api/v1/documents/{id}/content")
internal class ReplaceDocumentController(
    private val handler: DocumentHandler,
) {
    /**
     * Заменяет содержимое документа.
     * Новое содержимое передаётся в теле запроса в кодировке base64.
     *
     * @param id идентификатор документа
     * @param body данные для замены (содержимое в base64)
     * @return 200 с заменённым документом, 400 при ошибке валидации, или 404 если документ не найден
     */
    @PutMapping
    suspend fun replace(
        @PathVariable("id") id: String,
        @RequestBody body: DocumentHandler.ReplaceDocumentBody,
    ): ResponseEntity<*> {
        val request =
            DocumentHandler.ReplaceDocumentRequest(
                documentId = id,
                contentBase64 = body.contentBase64,
                fileName = body.fileName,
                contentType = body.contentType,
            )
        val result = handler.replace(request)
        return when (result) {
            is DocumentHandler.ReplaceDocumentResult.Success ->
                ResponseEntity.ok(result.document)
            DocumentHandler.ReplaceDocumentResult.NotFound ->
                ResponseEntity.notFound().build<Any>()
            is DocumentHandler.ReplaceDocumentResult.Failure ->
                ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(mapOf("reason" to result.reason))
        }
    }
}
