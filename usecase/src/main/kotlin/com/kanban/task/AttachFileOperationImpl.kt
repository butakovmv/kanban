package com.kanban.task

import com.kanban.common.FileAttachmentId
import com.kanban.common.TaskId
import java.time.Instant
import java.util.UUID

/**
 * Реализация операции прикрепления файла к задаче.
 * Проверяет существование задачи, загружает содержимое во внешнее хранилище через [FileStorage]
 * и сохраняет запись о прикреплении в репозиторий.
 */
internal class AttachFileOperationImpl(
    private val taskRepository: TaskRepository,
    private val fileAttachmentRepository: FileAttachmentRepository,
    private val fileStorage: FileStorage,
) : AttachFileOperation {
    override suspend fun execute(arg: AttachFileOperation.Arg): AttachFileOperation.Result {
        val validation = validate(arg)
        if (validation != null) return validation

        val fileId = FileAttachmentId(UUID.randomUUID().toString())
        val storageKey = "tasks/${arg.taskId}/${fileId.value}/${arg.fileName}"
        val storedKey = fileStorage.upload(storageKey, arg.content, arg.contentType)

        val attachment =
            FileAttachment(
                id = fileId,
                taskId = TaskId(arg.taskId),
                fileName = arg.fileName,
                contentType = arg.contentType,
                sizeBytes = arg.sizeBytes,
                storageKey = storedKey,
                uploadedBy = arg.uploadedBy,
                uploadedAt = Instant.now(),
            )
        val saved = fileAttachmentRepository.save(attachment)
        return AttachFileOperation.Result.Success(saved)
    }

    private suspend fun validate(arg: AttachFileOperation.Arg): AttachFileOperation.Result.Failure? =
        when {
            arg.fileName.isBlank() ->
                AttachFileOperation.Result.Failure("File name must not be blank")
            arg.contentType.isBlank() ->
                AttachFileOperation.Result.Failure("Content type must not be blank")
            arg.sizeBytes < 0 ->
                AttachFileOperation.Result.Failure("File size must not be negative")
            taskRepository.findById(arg.taskId) == null ->
                AttachFileOperation.Result.Failure("Task not found")
            else -> null
        }
}
