package com.kanban.project

internal class RemoveProjectMemberOperationImpl(
    private val projectRepository: ProjectRepository,
) : RemoveProjectMemberOperation {
    override suspend fun execute(arg: RemoveProjectMemberOperation.Arg): RemoveProjectMemberOperation.Result {
        val project = projectRepository.findById(arg.projectId)
        if (project == null) return RemoveProjectMemberOperation.Result.ProjectNotFound
        projectRepository.removeMember(arg.projectId, arg.userId)
        return RemoveProjectMemberOperation.Result.Success
    }
}
