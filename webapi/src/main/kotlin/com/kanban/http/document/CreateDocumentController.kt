package com.kanban.http.document

import com.fasterxml.jackson.annotation.JsonProperty
import com.kanban.document.DocumentHandler
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Контроллер создания документа.
 * Обрабатывает только запрос `POST /api/v1/documents`.
 *
 * @property handler обработчик запросов документов
 */
@RestController
@RequestMapping("/api/v1/documents")
internal class CreateDocumentController(
    private val handler: DocumentHandler,
) {
    /**
     * Создаёт новый документ.
     * Содержимое передаётся в теле запроса в кодировке base64.
     *
     * @param body данные для создания документа (содержимое в base64)
     * @return 201 с созданным документом, или 400 при ошибке
     */
    @PostMapping
    suspend fun create(
        @RequestBody body: CreateDocumentBody,
    ): ResponseEntity<*> {
        val request =
            DocumentHandler.CreateDocumentRequest(
                projectId = body.projectId,
                title = body.title,
                description = body.description,
                fileName = body.fileName,
                contentType = body.contentType,
                contentBase64 = body.contentBase64,
                uploadedBy = body.uploadedBy,
            )
        val result = handler.create(request)
        return when (result) {
            is DocumentHandler.CreateDocumentResult.Success ->
                ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(result.document)
            is DocumentHandler.CreateDocumentResult.Failure ->
                ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(mapOf("reason" to result.reason))
        }
    }

    /**
     * Тело запроса создания документа.
     *
     * @property projectId идентификатор проекта
     * @property title заголовок документа
     * @property description описание документа (опционально)
     * @property fileName имя файла
     * @property contentType MIME-тип содержимого
     * @property contentBase64 содержимое файла в кодировке base64
     * @property uploadedBy идентификатор пользователя, загрузившего документ
     */
    data class CreateDocumentBody(
        @JsonProperty("project_id")
        val projectId: String,
        val title: String,
        val description: String?,
        @JsonProperty("file_name")
        val fileName: String,
        @JsonProperty("content_type")
        val contentType: String,
        @JsonProperty("content_base64")
        val contentBase64: String,
        @JsonProperty("uploaded_by")
        val uploadedBy: String,
    )
}
