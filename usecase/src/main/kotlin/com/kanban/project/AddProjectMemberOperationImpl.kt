package com.kanban.project

internal class AddProjectMemberOperationImpl(
    private val projectRepository: ProjectRepository,
) : AddProjectMemberOperation {
    override suspend fun execute(arg: AddProjectMemberOperation.Arg): AddProjectMemberOperation.Result {
        val project = projectRepository.findById(arg.projectId)
        if (project == null) return AddProjectMemberOperation.Result.ProjectNotFound
        projectRepository.addMember(arg.projectId, arg.userId)
        return AddProjectMemberOperation.Result.Success
    }
}
