package com.kanban.task

/**
 * Реализация операции удаления прикреплённого файла.
 * Находит прикрепление по ID, удаляет объект из внешнего хранилища и запись из репозитория.
 */
internal class DeleteFileOperationImpl(
    private val fileAttachmentRepository: FileAttachmentRepository,
    private val fileStorage: FileStorage,
) : DeleteFileOperation {
    override suspend fun execute(arg: DeleteFileOperation.Arg): DeleteFileOperation.Result {
        val existing =
            fileAttachmentRepository.findById(arg.fileId) ?: return DeleteFileOperation.Result.NotFound
        fileStorage.delete(existing.storageKey)
        fileAttachmentRepository.delete(existing.id.value)
        return DeleteFileOperation.Result.Success
    }
}
