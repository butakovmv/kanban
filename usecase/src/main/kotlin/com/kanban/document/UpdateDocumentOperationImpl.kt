package com.kanban.document

import java.time.Instant

/**
 * Реализация операции обновления метаданных документа.
 * Находит документ по ID, обновляет указанные поля (title, description) и сохраняет.
 * Версия документа не изменяется, так как меняются только метаданные.
 */
internal class UpdateDocumentOperationImpl(
    private val documentRepository: DocumentRepository,
) : UpdateDocumentOperation {
    override suspend fun execute(arg: UpdateDocumentOperation.Arg): UpdateDocumentOperation.Result {
        val existing =
            documentRepository.findById(arg.documentId) ?: return UpdateDocumentOperation.Result.NotFound
        if (arg.title != null && arg.title.isBlank()) {
            return UpdateDocumentOperation.Result.Failure("Title must not be blank")
        }
        val updated =
            existing.copy(
                title = arg.title?.trim() ?: existing.title,
                description = arg.description ?: existing.description,
                updatedAt = Instant.now(),
            )
        val saved = documentRepository.save(updated)
        return UpdateDocumentOperation.Result.Success(saved)
    }
}
