package com.kanban.project

/**
 * Реализация операции получения списка проектов пользователя.
 * Делегирует запрос в репозиторий проектов.
 */
internal class ListProjectsOperationImpl(
    private val projectRepository: ProjectRepository,
) : ListProjectsOperation {
    override suspend fun execute(arg: ListProjectsOperation.Arg): ListProjectsOperation.Result {
        val projects = projectRepository.listByOwnerId(arg.ownerId)
        return ListProjectsOperation.Result.Success(projects)
    }
}
