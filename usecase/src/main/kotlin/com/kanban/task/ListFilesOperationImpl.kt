package com.kanban.task

/**
 * Реализация операции получения списка прикреплённых к задаче файлов.
 * Делегирует запрос в репозиторий прикреплений.
 */
internal class ListFilesOperationImpl(
    private val fileAttachmentRepository: FileAttachmentRepository,
) : ListFilesOperation {
    override suspend fun execute(arg: ListFilesOperation.Arg): ListFilesOperation.Result {
        val files = fileAttachmentRepository.listByTaskId(arg.taskId)
        return ListFilesOperation.Result.Success(files)
    }
}
