package com.kanban.project

/**
 * Реализация операции получения проекта.
 * Делегирует поиск в репозиторий проектов.
 */
internal class GetProjectOperationImpl(
    private val projectRepository: ProjectRepository,
) : GetProjectOperation {
    override suspend fun execute(arg: GetProjectOperation.Arg): GetProjectOperation.Result {
        val project = projectRepository.findById(arg.projectId) ?: return GetProjectOperation.Result.NotFound
        return GetProjectOperation.Result.Success(project)
    }
}
