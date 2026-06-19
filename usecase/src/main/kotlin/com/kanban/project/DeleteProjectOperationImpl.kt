package com.kanban.project

/**
 * Реализация операции удаления проекта.
 * Находит проект по ID, при отсутствии возвращает NotFound, иначе удаляет.
 */
internal class DeleteProjectOperationImpl(
    private val projectRepository: ProjectRepository,
) : DeleteProjectOperation {
    override suspend fun execute(arg: DeleteProjectOperation.Arg): DeleteProjectOperation.Result {
        val existing = projectRepository.findById(arg.projectId) ?: return DeleteProjectOperation.Result.NotFound
        projectRepository.delete(existing.id.value)
        return DeleteProjectOperation.Result.Success
    }
}
