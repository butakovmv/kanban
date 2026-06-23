package com.kanban.document

import com.kanban.common.DocumentId
import com.kanban.common.ProjectId
import com.kanban.project.ProjectRepository
import java.time.Instant
import java.util.UUID

internal class CreateDocumentOperationImpl(
    private val documentRepository: DocumentRepository,
    private val projectRepository: ProjectRepository,
) : CreateDocumentOperation {
    override suspend fun execute(arg: CreateDocumentOperation.Arg): CreateDocumentOperation.Result {
        val validation = validate(arg)
        if (validation != null) return validation

        val now = Instant.now()
        val document =
            Document(
                id = DocumentId(UUID.randomUUID().toString()),
                projectId = ProjectId(arg.projectId),
                path = arg.path.trim(),
                title = arg.title.trim(),
                content = arg.content,
                description = arg.description,
                createdAt = now,
                updatedAt = now,
            )
        val saved = documentRepository.save(document)
        return CreateDocumentOperation.Result.Success(saved)
    }

    private suspend fun validate(arg: CreateDocumentOperation.Arg): CreateDocumentOperation.Result.Failure? =
        when {
            arg.path.isBlank() ->
                CreateDocumentOperation.Result.Failure("Path must not be blank")
            arg.title.isBlank() ->
                CreateDocumentOperation.Result.Failure("Title must not be blank")
            projectRepository.findById(arg.projectId) == null ->
                CreateDocumentOperation.Result.Failure("Project not found")
            else -> null
        }
}
