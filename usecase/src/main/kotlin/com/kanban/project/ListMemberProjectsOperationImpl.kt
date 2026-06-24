package com.kanban.project

internal class ListMemberProjectsOperationImpl(
    private val projectRepository: ProjectRepository,
) : ListMemberProjectsOperation {
    override suspend fun execute(arg: ListMemberProjectsOperation.Arg): ListMemberProjectsOperation.Result {
        val projects = projectRepository.listByMemberId(arg.userId)
        return ListMemberProjectsOperation.Result.Success(projects)
    }
}
