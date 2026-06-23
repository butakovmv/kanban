package com.kanban.document

import java.time.Instant

internal class UpdateDocumentOperationImpl(
    private val documentRepository: DocumentRepository,
) : UpdateDocumentOperation {
    override suspend fun execute(arg: UpdateDocumentOperation.Arg): UpdateDocumentOperation.Result {
        val existing =
            documentRepository.findById(arg.documentId) ?: return UpdateDocumentOperation.Result.NotFound
        if (arg.title != null && arg.title.isBlank()) {
            return UpdateDocumentOperation.Result.Failure("Title must not be blank")
        }
        if (arg.path != null && arg.path.isBlank()) {
            return UpdateDocumentOperation.Result.Failure("Path must not be blank")
        }
        val updated =
            existing.copy(
                path = arg.path?.trim() ?: existing.path,
                title = arg.title?.trim() ?: existing.title,
                content = arg.content ?: existing.content,
                description = arg.description ?: existing.description,
                updatedAt = Instant.now(),
            )
        val saved = documentRepository.save(updated)
        return UpdateDocumentOperation.Result.Success(saved)
    }
}
