package com.kanban.document

import com.kanban.common.DocumentId
import com.kanban.common.ProjectId
import com.kanban.project.ProjectRepository
import java.time.Instant
import java.util.UUID

/**
 * Реализация операции создания документа.
 * Проверяет существование проекта, валидирует входные данные, загружает содержимое во внешнее хранилище
 * через [DocumentStorage] и сохраняет метаданные документа с начальной версией 1.
 */
internal class CreateDocumentOperationImpl(
    private val documentRepository: DocumentRepository,
    private val documentStorage: DocumentStorage,
    private val projectRepository: ProjectRepository,
) : CreateDocumentOperation {
    override suspend fun execute(arg: CreateDocumentOperation.Arg): CreateDocumentOperation.Result {
        val validation = validate(arg)
        if (validation != null) return validation

        val documentId = DocumentId(UUID.randomUUID().toString())
        val storageKey = "projects/${arg.projectId}/documents/${documentId.value}/${arg.fileName}"
        val storedKey = documentStorage.upload(storageKey, arg.content, arg.contentType)

        val now = Instant.now()
        val document =
            Document(
                id = documentId,
                projectId = ProjectId(arg.projectId),
                title = arg.title.trim(),
                description = arg.description,
                fileName = arg.fileName,
                contentType = arg.contentType,
                sizeBytes = arg.content.size.toLong(),
                storageKey = storedKey,
                version = 1,
                uploadedBy = arg.uploadedBy,
                createdAt = now,
                updatedAt = now,
            )
        val saved = documentRepository.save(document)
        return CreateDocumentOperation.Result.Success(saved)
    }

    private suspend fun validate(arg: CreateDocumentOperation.Arg): CreateDocumentOperation.Result.Failure? =
        when {
            arg.title.isBlank() ->
                CreateDocumentOperation.Result.Failure("Title must not be blank")
            arg.fileName.isBlank() ->
                CreateDocumentOperation.Result.Failure("File name must not be blank")
            arg.contentType.isBlank() ->
                CreateDocumentOperation.Result.Failure("Content type must not be blank")
            arg.content.isEmpty() ->
                CreateDocumentOperation.Result.Failure("Content must not be empty")
            projectRepository.findById(arg.projectId) == null ->
                CreateDocumentOperation.Result.Failure("Project not found")
            else -> null
        }
}
