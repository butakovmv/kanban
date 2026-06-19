package com.kanban.document

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant
import java.util.Base64
import kotlin.time.Duration.Companion.minutes

/**
 * Обработчик запросов документов.
 * Связывает HTTP-контроллеры с usecase-операциями: преобразует DTO в аргументы операций,
 * вызывает операции и преобразует результаты обратно в DTO.
 *
 * @property createDocumentOperation операция создания документа
 * @property getDocumentOperation операция получения документа
 * @property listDocumentsOperation операция получения списка документов проекта
 * @property updateDocumentOperation операция обновления метаданных документа
 * @property replaceDocumentOperation операция замены содержимого документа
 * @property deleteDocumentOperation операция удаления документа
 * @property documentStorage порт хранилища документов для генерации presigned-URL
 */
@Suppress("LongParameterList")
internal class DocumentHandler(
    private val createDocumentOperation: CreateDocumentOperation,
    private val getDocumentOperation: GetDocumentOperation,
    private val listDocumentsOperation: ListDocumentsOperation,
    private val updateDocumentOperation: UpdateDocumentOperation,
    private val replaceDocumentOperation: ReplaceDocumentOperation,
    private val deleteDocumentOperation: DeleteDocumentOperation,
    private val documentStorage: DocumentStorage,
) {
    /**
     * Создаёт новый документ в проекте.
     *
     * @param request данные для создания документа (содержимое в base64)
     * @return результат с созданным документом или ошибка
     */
    suspend fun create(request: CreateDocumentRequest): CreateDocumentResult {
        val content = decodeBase64(request.contentBase64)
        val result =
            createDocumentOperation.execute(
                CreateDocumentOperation.Arg(
                    projectId = request.projectId,
                    title = request.title,
                    description = request.description,
                    fileName = request.fileName,
                    contentType = request.contentType,
                    content = content,
                    uploadedBy = request.uploadedBy,
                ),
            )
        return when (result) {
            is CreateDocumentOperation.Result.Success ->
                CreateDocumentResult.Success(
                    document = result.document.toResponse(),
                )
            is CreateDocumentOperation.Result.Failure ->
                CreateDocumentResult.Failure(reason = result.reason)
        }
    }

    /**
     * Получает документ по идентификатору.
     *
     * @param request идентификатор документа
     * @return результат с документом или признак отсутствия
     */
    suspend fun get(request: GetDocumentRequest): GetDocumentResult {
        val result =
            getDocumentOperation.execute(
                GetDocumentOperation.Arg(documentId = request.documentId),
            )
        return when (result) {
            is GetDocumentOperation.Result.Success ->
                GetDocumentResult.Success(
                    document = result.document.toResponse(),
                )
            GetDocumentOperation.Result.NotFound -> GetDocumentResult.NotFound
        }
    }

    /**
     * Получает список документов проекта.
     *
     * @param request идентификатор проекта
     * @return результат со списком документов
     */
    suspend fun list(request: ListDocumentsRequest): ListDocumentsResult {
        val result =
            listDocumentsOperation.execute(
                ListDocumentsOperation.Arg(projectId = request.projectId),
            )
        return when (result) {
            is ListDocumentsOperation.Result.Success ->
                ListDocumentsResult.Success(
                    documents = result.documents.map { it.toResponse() },
                )
        }
    }

    /**
     * Обновляет метаданные документа.
     *
     * @param request данные для обновления
     * @return результат с обновлённым документом, ошибка валидации или признак отсутствия
     */
    suspend fun update(request: UpdateDocumentRequest): UpdateDocumentResult {
        val result =
            updateDocumentOperation.execute(
                UpdateDocumentOperation.Arg(
                    documentId = request.documentId,
                    title = request.title,
                    description = request.description,
                ),
            )
        return when (result) {
            is UpdateDocumentOperation.Result.Success ->
                UpdateDocumentResult.Success(
                    document = result.document.toResponse(),
                )
            UpdateDocumentOperation.Result.NotFound -> UpdateDocumentResult.NotFound
            is UpdateDocumentOperation.Result.Failure ->
                UpdateDocumentResult.Failure(reason = result.reason)
        }
    }

    /**
     * Заменяет содержимое документа.
     *
     * @param request данные для замены (содержимое в base64)
     * @return результат с заменённым документом, ошибка валидации или признак отсутствия
     */
    suspend fun replace(request: ReplaceDocumentRequest): ReplaceDocumentResult {
        val content = decodeBase64(request.contentBase64)
        val result =
            replaceDocumentOperation.execute(
                ReplaceDocumentOperation.Arg(
                    documentId = request.documentId,
                    content = content,
                    newFileName = request.fileName,
                    newContentType = request.contentType,
                ),
            )
        return when (result) {
            is ReplaceDocumentOperation.Result.Success ->
                ReplaceDocumentResult.Success(
                    document = result.document.toResponse(),
                )
            ReplaceDocumentOperation.Result.NotFound -> ReplaceDocumentResult.NotFound
            is ReplaceDocumentOperation.Result.Failure ->
                ReplaceDocumentResult.Failure(reason = result.reason)
        }
    }

    /**
     * Удаляет документ по идентификатору.
     *
     * @param request идентификатор документа
     * @return результат удаления
     */
    suspend fun delete(request: DeleteDocumentRequest): DeleteDocumentResult {
        val result =
            deleteDocumentOperation.execute(
                DeleteDocumentOperation.Arg(documentId = request.documentId),
            )
        return when (result) {
            DeleteDocumentOperation.Result.Success -> DeleteDocumentResult.Success
            DeleteDocumentOperation.Result.NotFound -> DeleteDocumentResult.NotFound
        }
    }

    /**
     * Генерирует presigned-URL для скачивания содержимого документа.
     *
     * @param request идентификатор документа
     * @return результат с URL или признак отсутствия
     */
    suspend fun getDownloadUrl(request: GetDocumentDownloadUrlRequest): GetDocumentDownloadUrlResult {
        val result =
            getDocumentOperation.execute(
                GetDocumentOperation.Arg(documentId = request.documentId),
            )
        return when (result) {
            is GetDocumentOperation.Result.Success -> {
                val url = documentStorage.getDownloadUrl(result.document.storageKey, DOWNLOAD_URL_TTL)
                GetDocumentDownloadUrlResult.Success(url = url)
            }
            GetDocumentOperation.Result.NotFound -> GetDocumentDownloadUrlResult.NotFound
        }
    }

    /**
     * Декодирует base64-строку в массив байт.
     */
    private fun decodeBase64(value: String): ByteArray = Base64.getDecoder().decode(value)

    /**
     * Преобразование сущности документа в DTO ответа.
     */
    private fun Document.toResponse(): DocumentResponse =
        DocumentResponse(
            id = id.value,
            projectId = projectId.value,
            title = title,
            description = description,
            fileName = fileName,
            contentType = contentType,
            sizeBytes = sizeBytes,
            storageKey = storageKey,
            version = version,
            uploadedBy = uploadedBy,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )

    /**
     * DTO запроса создания документа.
     *
     * @property projectId идентификатор проекта
     * @property title заголовок документа
     * @property description описание документа (опционально)
     * @property fileName имя файла
     * @property contentType MIME-тип содержимого
     * @property contentBase64 содержимое файла в кодировке base64
     * @property uploadedBy идентификатор пользователя, загрузившего документ
     */
    data class CreateDocumentRequest(
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

    /**
     * DTO запроса получения документа.
     *
     * @property documentId идентификатор документа
     */
    data class GetDocumentRequest(
        @JsonProperty("document_id")
        val documentId: String,
    )

    /**
     * DTO запроса списка документов проекта.
     *
     * @property projectId идентификатор проекта
     */
    data class ListDocumentsRequest(
        @JsonProperty("project_id")
        val projectId: String,
    )

    /**
     * DTO тела запроса обновления документа.
     *
     * @property title новый заголовок (null — не изменять)
     * @property description новое описание (null — не изменять)
     */
    data class UpdateDocumentBody(
        val title: String?,
        val description: String?,
    )

    /**
     * DTO запроса обновления документа (идентификатор берётся из пути).
     *
     * @property documentId идентификатор документа
     * @property title новый заголовок (null — не изменять)
     * @property description новое описание (null — не изменять)
     */
    data class UpdateDocumentRequest(
        @JsonProperty("document_id")
        val documentId: String,
        val title: String?,
        val description: String?,
    )

    /**
     * DTO тела запроса замены содержимого документа.
     *
     * @property contentBase64 новое содержимое файла в кодировке base64
     * @property fileName новое имя файла (опционально)
     * @property contentType новый MIME-тип содержимого (опционально)
     */
    data class ReplaceDocumentBody(
        @JsonProperty("content_base64")
        val contentBase64: String,
        @JsonProperty("file_name")
        val fileName: String?,
        @JsonProperty("content_type")
        val contentType: String?,
    )

    /**
     * DTO запроса замены содержимого документа (идентификатор берётся из пути).
     *
     * @property documentId идентификатор документа
     * @property contentBase64 новое содержимое файла в кодировке base64
     * @property fileName новое имя файла (опционально)
     * @property contentType новый MIME-тип содержимого (опционально)
     */
    data class ReplaceDocumentRequest(
        @JsonProperty("document_id")
        val documentId: String,
        @JsonProperty("content_base64")
        val contentBase64: String,
        @JsonProperty("file_name")
        val fileName: String?,
        @JsonProperty("content_type")
        val contentType: String?,
    )

    /**
     * DTO запроса удаления документа.
     *
     * @property documentId идентификатор документа
     */
    data class DeleteDocumentRequest(
        @JsonProperty("document_id")
        val documentId: String,
    )

    /**
     * DTO запроса URL для скачивания документа.
     *
     * @property documentId идентификатор документа
     */
    data class GetDocumentDownloadUrlRequest(
        @JsonProperty("document_id")
        val documentId: String,
    )

    /**
     * DTO ответа с документом.
     *
     * @property id идентификатор документа
     * @property projectId идентификатор проекта
     * @property title заголовок документа
     * @property description описание документа
     * @property fileName имя файла
     * @property contentType MIME-тип содержимого
     * @property sizeBytes размер файла в байтах
     * @property storageKey ключ (путь) объекта во внешнем хранилище
     * @property version номер версии содержимого
     * @property uploadedBy идентификатор пользователя, загрузившего документ
     * @property createdAt дата создания
     * @property updatedAt дата последнего изменения
     */
    data class DocumentResponse(
        val id: String,
        @JsonProperty("project_id")
        val projectId: String,
        val title: String,
        val description: String?,
        @JsonProperty("file_name")
        val fileName: String,
        @JsonProperty("content_type")
        val contentType: String,
        @JsonProperty("size_bytes")
        val sizeBytes: Long,
        @JsonProperty("storage_key")
        val storageKey: String,
        val version: Int,
        @JsonProperty("uploaded_by")
        val uploadedBy: String,
        @JsonProperty("created_at")
        val createdAt: Instant,
        @JsonProperty("updated_at")
        val updatedAt: Instant,
    )

    /**
     * Результат операции создания документа.
     */
    sealed interface CreateDocumentResult {
        /** Документ успешно создан. */
        data class Success(
            val document: DocumentResponse,
        ) : CreateDocumentResult

        /** Ошибка создания документа. */
        data class Failure(
            val reason: String,
        ) : CreateDocumentResult
    }

    /**
     * Результат операции получения документа.
     */
    sealed interface GetDocumentResult {
        /** Документ найден. */
        data class Success(
            val document: DocumentResponse,
        ) : GetDocumentResult

        /** Документ не найден. */
        data object NotFound : GetDocumentResult
    }

    /**
     * Результат операции получения списка документов.
     */
    sealed interface ListDocumentsResult {
        /** Список документов успешно получен. */
        data class Success(
            val documents: List<DocumentResponse>,
        ) : ListDocumentsResult
    }

    /**
     * Результат операции обновления документа.
     */
    sealed interface UpdateDocumentResult {
        /** Документ успешно обновлён. */
        data class Success(
            val document: DocumentResponse,
        ) : UpdateDocumentResult

        /** Документ не найден. */
        data object NotFound : UpdateDocumentResult

        /** Ошибка обновления. */
        data class Failure(
            val reason: String,
        ) : UpdateDocumentResult
    }

    /**
     * Результат операции замены содержимого документа.
     */
    sealed interface ReplaceDocumentResult {
        /** Содержимое успешно заменено. */
        data class Success(
            val document: DocumentResponse,
        ) : ReplaceDocumentResult

        /** Документ не найден. */
        data object NotFound : ReplaceDocumentResult

        /** Ошибка замены. */
        data class Failure(
            val reason: String,
        ) : ReplaceDocumentResult
    }

    /**
     * Результат операции удаления документа.
     */
    sealed interface DeleteDocumentResult {
        /** Документ успешно удалён. */
        data object Success : DeleteDocumentResult

        /** Документ не найден. */
        data object NotFound : DeleteDocumentResult
    }

    /**
     * Результат операции получения URL для скачивания документа.
     */
    sealed interface GetDocumentDownloadUrlResult {
        /** URL успешно получен. */
        data class Success(
            val url: String,
        ) : GetDocumentDownloadUrlResult

        /** Документ не найден. */
        data object NotFound : GetDocumentDownloadUrlResult
    }

    companion object {
        /**
         * Срок действия presigned-URL для скачивания документа.
         */
        private val DOWNLOAD_URL_TTL = 15.minutes
    }
}
