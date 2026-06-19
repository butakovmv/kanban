package com.kanban.document

/**
 * Реализация операции получения списка документов проекта.
 * Делегирует запрос в репозиторий документов.
 */
internal class ListDocumentsOperationImpl(
    private val documentRepository: DocumentRepository,
) : ListDocumentsOperation {
    override suspend fun execute(arg: ListDocumentsOperation.Arg): ListDocumentsOperation.Result {
        val documents = documentRepository.listByProjectId(arg.projectId)
        return ListDocumentsOperation.Result.Success(documents)
    }
}
