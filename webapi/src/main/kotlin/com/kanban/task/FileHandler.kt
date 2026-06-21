package com.kanban.task

import java.time.Instant
import java.util.Base64

internal class FileHandler(
    private val attachFileOperation: AttachFileOperation,
    private val deleteFileOperation: DeleteFileOperation,
    private val listFilesOperation: ListFilesOperation,
    private val getFileDownloadUrlOperation: GetFileDownloadUrlOperation,
) {
    data class FileData(
        val id: String,
        val taskId: String,
        val fileName: String,
        val contentType: String,
        val sizeBytes: Long,
        val storageKey: String,
        val uploadedBy: String,
        val uploadedAt: Instant,
    )

    suspend fun attach(
        taskId: String,
        fileName: String,
        contentType: String,
        contentBase64: String,
        uploadedBy: String,
    ): AttachFileResult {
        val content = decodeBase64(contentBase64)
        val sizeBytes = content.size.toLong()
        val result =
            attachFileOperation.execute(
                AttachFileOperation.Arg(
                    taskId = taskId,
                    fileName = fileName,
                    contentType = contentType,
                    sizeBytes = sizeBytes,
                    content = content,
                    uploadedBy = uploadedBy,
                ),
            )
        return when (result) {
            is AttachFileOperation.Result.Success ->
                AttachFileResult.Success(
                    file = result.file.toData(),
                )
            is AttachFileOperation.Result.Failure ->
                AttachFileResult.Failure(reason = result.reason)
        }
    }

    suspend fun delete(fileId: String): DeleteFileResult {
        val result =
            deleteFileOperation.execute(
                DeleteFileOperation.Arg(fileId = fileId),
            )
        return when (result) {
            DeleteFileOperation.Result.Success -> DeleteFileResult.Success
            DeleteFileOperation.Result.NotFound -> DeleteFileResult.NotFound
        }
    }

    suspend fun list(taskId: String): ListFilesResult {
        val result =
            listFilesOperation.execute(
                ListFilesOperation.Arg(taskId = taskId),
            )
        return when (result) {
            is ListFilesOperation.Result.Success ->
                ListFilesResult.Success(
                    files = result.files.map { it.toData() },
                )
        }
    }

    suspend fun getDownloadUrl(fileId: String): GetFileDownloadUrlResult {
        val result =
            getFileDownloadUrlOperation.execute(
                GetFileDownloadUrlOperation.Arg(fileId = fileId),
            )
        return when (result) {
            is GetFileDownloadUrlOperation.Result.Success ->
                GetFileDownloadUrlResult.Success(
                    url = result.url,
                )
            GetFileDownloadUrlOperation.Result.NotFound -> GetFileDownloadUrlResult.NotFound
        }
    }

    private fun decodeBase64(value: String): ByteArray = Base64.getDecoder().decode(value)

    private fun FileAttachment.toData(): FileData =
        FileData(
            id = id.value,
            taskId = taskId.value,
            fileName = fileName,
            contentType = contentType,
            sizeBytes = sizeBytes,
            storageKey = storageKey,
            uploadedBy = uploadedBy,
            uploadedAt = uploadedAt,
        )

    sealed interface AttachFileResult {
        data class Success(
            val file: FileData,
        ) : AttachFileResult

        data class Failure(
            val reason: String,
        ) : AttachFileResult
    }

    sealed interface DeleteFileResult {
        data object Success : DeleteFileResult

        data object NotFound : DeleteFileResult
    }

    sealed interface ListFilesResult {
        data class Success(
            val files: List<FileData>,
        ) : ListFilesResult
    }

    sealed interface GetFileDownloadUrlResult {
        data class Success(
            val url: String,
        ) : GetFileDownloadUrlResult

        data object NotFound : GetFileDownloadUrlResult
    }
}
