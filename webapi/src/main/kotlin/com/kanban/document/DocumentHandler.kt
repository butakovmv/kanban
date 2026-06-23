package com.kanban.document

import java.time.Instant

internal class DocumentHandler(
    private val createDocumentOperation: CreateDocumentOperation,
    private val getDocumentOperation: GetDocumentOperation,
    private val listDocumentsOperation: ListDocumentsOperation,
    private val updateDocumentOperation: UpdateDocumentOperation,
    private val deleteDocumentOperation: DeleteDocumentOperation,
) {
    data class DocumentData(
        val id: String,
        val projectId: String,
        val path: String,
        val title: String,
        val content: String = "",
        val description: String?,
        val createdAt: Instant,
        val updatedAt: Instant,
    )

    suspend fun create(
        projectId: String,
        path: String,
        title: String,
        content: String,
        description: String?,
    ): CreateDocumentResult {
        val result =
            createDocumentOperation.execute(
                CreateDocumentOperation.Arg(
                    projectId = projectId,
                    path = path,
                    title = title,
                    content = content,
                    description = description,
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
        path: String?,
        title: String?,
        content: String?,
        description: String?,
    ): UpdateDocumentResult {
        val result =
            updateDocumentOperation.execute(
                UpdateDocumentOperation.Arg(
                    documentId = documentId,
                    path = path,
                    title = title,
                    content = content,
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

    private fun Document.toData() =
        DocumentData(
            id = id.value,
            projectId = projectId.value,
            path = path,
            title = title,
            content = content,
            description = description,
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

    sealed interface DeleteDocumentResult {
        data object Success : DeleteDocumentResult

        data object NotFound : DeleteDocumentResult
    }
}
