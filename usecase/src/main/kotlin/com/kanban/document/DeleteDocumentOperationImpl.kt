package com.kanban.document

internal class DeleteDocumentOperationImpl(
    private val documentRepository: DocumentRepository,
) : DeleteDocumentOperation {
    override suspend fun execute(arg: DeleteDocumentOperation.Arg): DeleteDocumentOperation.Result {
        val existing =
            documentRepository.findById(arg.documentId) ?: return DeleteDocumentOperation.Result.NotFound
        documentRepository.delete(existing.id.value)
        return DeleteDocumentOperation.Result.Success
    }
}
