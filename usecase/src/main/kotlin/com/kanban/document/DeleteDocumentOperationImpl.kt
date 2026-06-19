package com.kanban.document

/**
 * Реализация операции удаления документа.
 * Находит документ по ID, удаляет объект из внешнего хранилища и запись из репозитория.
 */
internal class DeleteDocumentOperationImpl(
    private val documentRepository: DocumentRepository,
    private val documentStorage: DocumentStorage,
) : DeleteDocumentOperation {
    override suspend fun execute(arg: DeleteDocumentOperation.Arg): DeleteDocumentOperation.Result {
        val existing =
            documentRepository.findById(arg.documentId) ?: return DeleteDocumentOperation.Result.NotFound
        documentStorage.delete(existing.storageKey)
        documentRepository.delete(existing.id.value)
        return DeleteDocumentOperation.Result.Success
    }
}
