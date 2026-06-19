package com.kanban.document

import java.time.Instant

/**
 * Реализация операции замены содержимого документа.
 * Находит документ по ID, загружает новое содержимое во внешнее хранилище через [DocumentStorage],
 * удаляет предыдущий объект, обновляет имя файла и/или MIME-тип (если переданы) и увеличивает номер версии.
 */
internal class ReplaceDocumentOperationImpl(
    private val documentRepository: DocumentRepository,
    private val documentStorage: DocumentStorage,
) : ReplaceDocumentOperation {
    override suspend fun execute(arg: ReplaceDocumentOperation.Arg): ReplaceDocumentOperation.Result {
        val existing =
            documentRepository.findById(arg.documentId) ?: return ReplaceDocumentOperation.Result.NotFound
        val validation = validate(arg)
        if (validation != null) return validation

        val newFileName = arg.newFileName?.trim() ?: existing.fileName
        val newContentType = arg.newContentType?.trim() ?: existing.contentType
        val newKey = "projects/${existing.projectId.value}/documents/${existing.id.value}/$newFileName"
        val storedKey = documentStorage.upload(newKey, arg.content, newContentType)

        documentStorage.delete(existing.storageKey)

        val updated =
            existing.copy(
                fileName = newFileName,
                contentType = newContentType,
                sizeBytes = arg.content.size.toLong(),
                storageKey = storedKey,
                version = existing.version + 1,
                updatedAt = Instant.now(),
            )
        val saved = documentRepository.save(updated)
        return ReplaceDocumentOperation.Result.Success(saved)
    }

    private fun validate(arg: ReplaceDocumentOperation.Arg): ReplaceDocumentOperation.Result.Failure? =
        when {
            arg.content.isEmpty() ->
                ReplaceDocumentOperation.Result.Failure("Content must not be empty")
            arg.newFileName != null && arg.newFileName.isBlank() ->
                ReplaceDocumentOperation.Result.Failure("File name must not be blank")
            arg.newContentType != null && arg.newContentType.isBlank() ->
                ReplaceDocumentOperation.Result.Failure("Content type must not be blank")
            else -> null
        }
}
