package com.kanban.document

import java.time.Instant
import java.util.Base64
import kotlin.time.Duration.Companion.minutes

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
    data class DocumentData(
        val id: String,
        val projectId: String,
        val title: String,
        val description: String?,
        val fileName: String,
        val contentType: String,
        val sizeBytes: Long,
        val storageKey: String,
        val version: Int,
        val uploadedBy: String,
        val createdAt: Instant,
        val updatedAt: Instant,
    )

    suspend fun create(
        projectId: String,
        title: String,
        description: String?,
        fileName: String,
        contentType: String,
        contentBase64: String,
        uploadedBy: String,
    ): CreateDocumentResult {
        val content = decodeBase64(contentBase64)
        val result =
            createDocumentOperation.execute(
                CreateDocumentOperation.Arg(
                    projectId = projectId,
                    title = title,
                    description = description,
                    fileName = fileName,
                    contentType = contentType,
                    content = content,
                    uploadedBy = uploadedBy,
                ),
            )
        return when (result) {
            is CreateDocumentOperation.Result.Success ->
                CreateDocumentResult.Success(
                    document = result.document.toData(),
                )
            is CreateDocumentOperation.Result.Failure ->
                CreateDocumentResult.Failure(reason = result.reason)
        }
    }

    suspend fun get(documentId: String): GetDocumentResult {
        val result =
            getDocumentOperation.execute(
                GetDocumentOperation.Arg(documentId = documentId),
            )
        return when (result) {
            is GetDocumentOperation.Result.Success ->
                GetDocumentResult.Success(
                    document = result.document.toData(),
                )
            GetDocumentOperation.Result.NotFound -> GetDocumentResult.NotFound
        }
    }

    suspend fun list(projectId: String): ListDocumentsResult {
        val result =
            listDocumentsOperation.execute(
                ListDocumentsOperation.Arg(projectId = projectId),
            )
        return when (result) {
            is ListDocumentsOperation.Result.Success ->
                ListDocumentsResult.Success(
                    documents = result.documents.map { it.toData() },
                )
        }
    }

    suspend fun update(
        documentId: String,
        title: String?,
        description: String?,
    ): UpdateDocumentResult {
        val result =
            updateDocumentOperation.execute(
                UpdateDocumentOperation.Arg(
                    documentId = documentId,
                    title = title,
                    description = description,
                ),
            )
        return when (result) {
            is UpdateDocumentOperation.Result.Success ->
                UpdateDocumentResult.Success(
                    document = result.document.toData(),
                )
            UpdateDocumentOperation.Result.NotFound -> UpdateDocumentResult.NotFound
            is UpdateDocumentOperation.Result.Failure ->
                UpdateDocumentResult.Failure(reason = result.reason)
        }
    }

    suspend fun replace(
        documentId: String,
        contentBase64: String,
        fileName: String?,
        contentType: String?,
    ): ReplaceDocumentResult {
        val content = decodeBase64(contentBase64)
        val result =
            replaceDocumentOperation.execute(
                ReplaceDocumentOperation.Arg(
                    documentId = documentId,
                    content = content,
                    newFileName = fileName,
                    newContentType = contentType,
                ),
            )
        return when (result) {
            is ReplaceDocumentOperation.Result.Success ->
                ReplaceDocumentResult.Success(
                    document = result.document.toData(),
                )
            ReplaceDocumentOperation.Result.NotFound -> ReplaceDocumentResult.NotFound
            is ReplaceDocumentOperation.Result.Failure ->
                ReplaceDocumentResult.Failure(reason = result.reason)
        }
    }

    suspend fun delete(documentId: String): DeleteDocumentResult {
        val result =
            deleteDocumentOperation.execute(
                DeleteDocumentOperation.Arg(documentId = documentId),
            )
        return when (result) {
            DeleteDocumentOperation.Result.Success -> DeleteDocumentResult.Success
            DeleteDocumentOperation.Result.NotFound -> DeleteDocumentResult.NotFound
        }
    }

    suspend fun getDownloadUrl(documentId: String): GetDocumentDownloadUrlResult {
        val result =
            getDocumentOperation.execute(
                GetDocumentOperation.Arg(documentId = documentId),
            )
        return when (result) {
            is GetDocumentOperation.Result.Success -> {
                val url = documentStorage.getDownloadUrl(result.document.storageKey, DOWNLOAD_URL_TTL)
                GetDocumentDownloadUrlResult.Success(url = url)
            }
            GetDocumentOperation.Result.NotFound -> GetDocumentDownloadUrlResult.NotFound
        }
    }

    private fun decodeBase64(value: String): ByteArray = Base64.getDecoder().decode(value)

    private fun Document.toData(): DocumentData =
        DocumentData(
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

    sealed interface CreateDocumentResult {
        data class Success(
            val document: DocumentData,
        ) : CreateDocumentResult

        data class Failure(
            val reason: String,
        ) : CreateDocumentResult
    }

    sealed interface GetDocumentResult {
        data class Success(
            val document: DocumentData,
        ) : GetDocumentResult

        data object NotFound : GetDocumentResult
    }

    sealed interface ListDocumentsResult {
        data class Success(
            val documents: List<DocumentData>,
        ) : ListDocumentsResult
    }

    sealed interface UpdateDocumentResult {
        data class Success(
            val document: DocumentData,
        ) : UpdateDocumentResult

        data object NotFound : UpdateDocumentResult

        data class Failure(
            val reason: String,
        ) : UpdateDocumentResult
    }

    sealed interface ReplaceDocumentResult {
        data class Success(
            val document: DocumentData,
        ) : ReplaceDocumentResult

        data object NotFound : ReplaceDocumentResult

        data class Failure(
            val reason: String,
        ) : ReplaceDocumentResult
    }

    sealed interface DeleteDocumentResult {
        data object Success : DeleteDocumentResult

        data object NotFound : DeleteDocumentResult
    }

    sealed interface GetDocumentDownloadUrlResult {
        data class Success(
            val url: String,
        ) : GetDocumentDownloadUrlResult

        data object NotFound : GetDocumentDownloadUrlResult
    }

    companion object {
        private val DOWNLOAD_URL_TTL = 15.minutes
    }
}
