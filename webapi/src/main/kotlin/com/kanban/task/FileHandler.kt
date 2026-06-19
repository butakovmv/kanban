package com.kanban.task

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant
import java.util.Base64

/**
 * Обработчик запросов прикреплённых файлов.
 * Связывает HTTP-контроллеры с usecase-операциями: преобразует DTO в аргументы операций,
 * вызывает операции и преобразует результаты обратно в DTO.
 *
 * @property attachFileOperation операция прикрепления файла
 * @property deleteFileOperation операция удаления файла
 * @property listFilesOperation операция получения списка файлов
 * @property getFileDownloadUrlOperation операция получения URL для скачивания
 */
internal class FileHandler(
    private val attachFileOperation: AttachFileOperation,
    private val deleteFileOperation: DeleteFileOperation,
    private val listFilesOperation: ListFilesOperation,
    private val getFileDownloadUrlOperation: GetFileDownloadUrlOperation,
) {
    /**
     * Прикрепляет файл к задаче.
     *
     * @param request данные для прикрепления файла (содержимое в base64)
     * @return результат с созданным прикреплением или ошибка
     */
    suspend fun attach(request: AttachFileRequest): AttachFileResult {
        val content = decodeBase64(request.contentBase64)
        val sizeBytes = content.size.toLong()
        val result =
            attachFileOperation.execute(
                AttachFileOperation.Arg(
                    taskId = request.taskId,
                    fileName = request.fileName,
                    contentType = request.contentType,
                    sizeBytes = sizeBytes,
                    content = content,
                    uploadedBy = request.uploadedBy,
                ),
            )
        return when (result) {
            is AttachFileOperation.Result.Success ->
                AttachFileResult.Success(
                    file = result.file.toResponse(),
                )
            is AttachFileOperation.Result.Failure ->
                AttachFileResult.Failure(reason = result.reason)
        }
    }

    /**
     * Удаляет прикреплённый файл.
     *
     * @param request идентификатор прикрепления
     * @return результат удаления
     */
    suspend fun delete(request: DeleteFileRequest): DeleteFileResult {
        val result =
            deleteFileOperation.execute(
                DeleteFileOperation.Arg(fileId = request.fileId),
            )
        return when (result) {
            DeleteFileOperation.Result.Success -> DeleteFileResult.Success
            DeleteFileOperation.Result.NotFound -> DeleteFileResult.NotFound
        }
    }

    /**
     * Получает список файлов задачи.
     *
     * @param request идентификатор задачи
     * @return результат со списком файлов
     */
    suspend fun list(request: ListFilesRequest): ListFilesResult {
        val result =
            listFilesOperation.execute(
                ListFilesOperation.Arg(taskId = request.taskId),
            )
        return when (result) {
            is ListFilesOperation.Result.Success ->
                ListFilesResult.Success(
                    files = result.files.map { it.toResponse() },
                )
        }
    }

    /**
     * Получает presigned-URL для скачивания файла.
     *
     * @param request идентификатор прикрепления
     * @return результат с URL или признак отсутствия
     */
    suspend fun getDownloadUrl(request: GetFileDownloadUrlRequest): GetFileDownloadUrlResult {
        val result =
            getFileDownloadUrlOperation.execute(
                GetFileDownloadUrlOperation.Arg(fileId = request.fileId),
            )
        return when (result) {
            is GetFileDownloadUrlOperation.Result.Success ->
                GetFileDownloadUrlResult.Success(
                    url = result.url,
                )
            GetFileDownloadUrlOperation.Result.NotFound -> GetFileDownloadUrlResult.NotFound
        }
    }

    /**
     * Декодирует base64-строку в массив байт.
     */
    private fun decodeBase64(value: String): ByteArray = Base64.getDecoder().decode(value)

    /**
     * Преобразование сущности прикрепления в DTO ответа.
     */
    private fun FileAttachment.toResponse(): FileAttachmentResponse =
        FileAttachmentResponse(
            id = id.value,
            taskId = taskId.value,
            fileName = fileName,
            contentType = contentType,
            sizeBytes = sizeBytes,
            storageKey = storageKey,
            uploadedBy = uploadedBy,
            uploadedAt = uploadedAt,
        )

    /**
     * DTO запроса прикрепления файла.
     *
     * @property taskId идентификатор задачи
     * @property fileName имя файла
     * @property contentType MIME-тип содержимого
     * @property contentBase64 содержимое файла в кодировке base64
     * @property uploadedBy идентификатор пользователя, загрузившего файл
     */
    data class AttachFileRequest(
        @JsonProperty("task_id")
        val taskId: String,
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
     * DTO запроса удаления файла.
     *
     * @property fileId идентификатор прикрепления
     */
    data class DeleteFileRequest(
        @JsonProperty("file_id")
        val fileId: String,
    )

    /**
     * DTO запроса списка файлов задачи.
     *
     * @property taskId идентификатор задачи
     */
    data class ListFilesRequest(
        @JsonProperty("task_id")
        val taskId: String,
    )

    /**
     * DTO запроса URL для скачивания файла.
     *
     * @property fileId идентификатор прикрепления
     */
    data class GetFileDownloadUrlRequest(
        @JsonProperty("file_id")
        val fileId: String,
    )

    /**
     * DTO ответа с метаданными прикрепления.
     *
     * @property id идентификатор прикрепления
     * @property taskId идентификатор задачи
     * @property fileName имя файла
     * @property contentType MIME-тип содержимого
     * @property sizeBytes размер файла в байтах
     * @property storageKey ключ (путь) объекта во внешнем хранилище
     * @property uploadedBy идентификатор пользователя, загрузившего файл
     * @property uploadedAt дата и время загрузки
     */
    data class FileAttachmentResponse(
        val id: String,
        @JsonProperty("task_id")
        val taskId: String,
        @JsonProperty("file_name")
        val fileName: String,
        @JsonProperty("content_type")
        val contentType: String,
        @JsonProperty("size_bytes")
        val sizeBytes: Long,
        @JsonProperty("storage_key")
        val storageKey: String,
        @JsonProperty("uploaded_by")
        val uploadedBy: String,
        @JsonProperty("uploaded_at")
        val uploadedAt: Instant,
    )

    /**
     * DTO ответа с URL для скачивания.
     *
     * @property url presigned-URL для скачивания файла
     */
    data class DownloadUrlResponse(
        val url: String,
    )

    /**
     * Результат операции прикрепления файла.
     */
    sealed interface AttachFileResult {
        /** Файл успешно прикреплён. */
        data class Success(
            val file: FileAttachmentResponse,
        ) : AttachFileResult

        /** Ошибка прикрепления файла. */
        data class Failure(
            val reason: String,
        ) : AttachFileResult
    }

    /**
     * Результат операции удаления файла.
     */
    sealed interface DeleteFileResult {
        /** Файл успешно удалён. */
        data object Success : DeleteFileResult

        /** Файл не найден. */
        data object NotFound : DeleteFileResult
    }

    /**
     * Результат операции получения списка файлов.
     */
    sealed interface ListFilesResult {
        /** Список файлов успешно получен. */
        data class Success(
            val files: List<FileAttachmentResponse>,
        ) : ListFilesResult
    }

    /**
     * Результат операции получения URL для скачивания.
     */
    sealed interface GetFileDownloadUrlResult {
        /** URL успешно получен. */
        data class Success(
            val url: String,
        ) : GetFileDownloadUrlResult

        /** Файл не найден. */
        data object NotFound : GetFileDownloadUrlResult
    }
}
