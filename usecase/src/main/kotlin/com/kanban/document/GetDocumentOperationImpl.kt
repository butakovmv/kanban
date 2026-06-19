package com.kanban.document

/**
 * Реализация операции получения документа.
 * Делегирует поиск в репозиторий документов.
 */
internal class GetDocumentOperationImpl(
    private val documentRepository: DocumentRepository,
) : GetDocumentOperation {
    override suspend fun execute(arg: GetDocumentOperation.Arg): GetDocumentOperation.Result {
        val document =
            documentRepository.findById(arg.documentId) ?: return GetDocumentOperation.Result.NotFound
        return GetDocumentOperation.Result.Success(document)
    }
}
