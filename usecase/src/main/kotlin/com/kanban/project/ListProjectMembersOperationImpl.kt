package com.kanban.project

internal class ListProjectMembersOperationImpl(
    private val projectRepository: ProjectRepository,
) : ListProjectMembersOperation {
    override suspend fun execute(arg: ListProjectMembersOperation.Arg): ListProjectMembersOperation.Result {
        val members = projectRepository.findMembers(arg.projectId)
        return ListProjectMembersOperation.Result.Success(members)
    }
}
