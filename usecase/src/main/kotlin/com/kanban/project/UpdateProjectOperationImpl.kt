package com.kanban.project

import java.time.Instant

/**
 * Реализация операции обновления проекта.
 * Находит проект по ID, обновляет указанные поля (name, description) и сохраняет.
 */
internal class UpdateProjectOperationImpl(
    private val projectRepository: ProjectRepository,
) : UpdateProjectOperation {
    override suspend fun execute(arg: UpdateProjectOperation.Arg): UpdateProjectOperation.Result {
        val existing =
            projectRepository.findById(arg.projectId) ?: return UpdateProjectOperation.Result.NotFound

        val updated =
            existing.copy(
                name = arg.name ?: existing.name,
                description = arg.description ?: existing.description,
                updatedAt = Instant.now(),
            )
        val saved = projectRepository.save(updated)
        return UpdateProjectOperation.Result.Success(saved)
    }
}
