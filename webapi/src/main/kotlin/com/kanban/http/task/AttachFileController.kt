package com.kanban.http.task

import com.fasterxml.jackson.annotation.JsonProperty
import com.kanban.task.FileHandler
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Контроллер прикрепления файла к задаче.
 * Обрабатывает только запрос `POST /api/v1/tasks/{taskId}/files`.
 *
 * @property handler обработчик запросов прикреплённых файлов
 */
@RestController
@RequestMapping("/api/v1/tasks/{taskId}/files")
internal class AttachFileController(
    private val handler: FileHandler,
) {
    /**
     * Прикрепляет файл к задаче.
     * Содержимое передаётся в теле запроса в кодировке base64.
     *
     * @param taskId идентификатор задачи
     * @param body данные о файле (содержимое в base64)
     * @return 201 с метаданными прикреплённого файла, или 400 при ошибке
     */
    @PostMapping
    suspend fun attach(
        @PathVariable("taskId") taskId: String,
        @RequestBody body: AttachFileBody,
    ): ResponseEntity<*> {
        val request =
            FileHandler.AttachFileRequest(
                taskId = taskId,
                fileName = body.fileName,
                contentType = body.contentType,
                contentBase64 = body.contentBase64,
                uploadedBy = body.uploadedBy,
            )
        val result = handler.attach(request)
        return when (result) {
            is FileHandler.AttachFileResult.Success ->
                ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(result.file)
            is FileHandler.AttachFileResult.Failure ->
                ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(mapOf("reason" to result.reason))
        }
    }

    /**
     * Тело запроса прикрепления файла.
     *
     * @property fileName имя файла
     * @property contentType MIME-тип содержимого
     * @property contentBase64 содержимое файла в кодировке base64
     * @property uploadedBy идентификатор пользователя, загрузившего файл
     */
    data class AttachFileBody(
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
