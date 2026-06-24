package com.kanban.project

import com.kanban.audit.LogAuditEventOperation
import java.time.Instant

internal class ProjectHandler(
    private val createProjectOperation: CreateProjectOperation,
    private val getProjectOperation: GetProjectOperation,
    private val listProjectsOperation: ListProjectsOperation,
    private val updateProjectOperation: UpdateProjectOperation,
    private val deleteProjectOperation: DeleteProjectOperation,
    private val listProjectMembersOperation: ListProjectMembersOperation,
    private val addProjectMemberOperation: AddProjectMemberOperation,
    private val removeProjectMemberOperation: RemoveProjectMemberOperation,
    private val listMemberProjectsOperation: ListMemberProjectsOperation,
    private val logAuditEventOperation: LogAuditEventOperation,
) {
    data class ProjectData(
        val id: String,
        val ownerId: String,
        val name: String,
        val description: String?,
        val createdAt: Instant,
        val updatedAt: Instant,
    )

    suspend fun create(
        ownerId: String,
        name: String,
        description: String?,
    ): CreateProjectResult {
        val result =
            createProjectOperation.execute(
                CreateProjectOperation.Arg(
                    ownerId = ownerId,
                    name = name,
                    description = description,
                ),
            )
        return when (result) {
            is CreateProjectOperation.Result.Success -> {
                logAuditEventOperation.execute(
                    LogAuditEventOperation.Arg(
                        projectId = result.project.id.value,
                        documentId = null,
                        userId = ownerId,
                        action = "project.created",
                        details = "{\"name\":\"${name}\"}",
                    ),
                )
                CreateProjectResult.Success(
                    project = result.project.toData(),
                )
            }
            is CreateProjectOperation.Result.Failure ->
                CreateProjectResult.Failure(reason = result.reason)
        }
    }

    suspend fun get(projectId: String): GetProjectResult {
        val result =
            getProjectOperation.execute(
                GetProjectOperation.Arg(projectId = projectId),
            )
        return when (result) {
            is GetProjectOperation.Result.Success ->
                GetProjectResult.Success(
                    project = result.project.toData(),
                )
            GetProjectOperation.Result.NotFound -> GetProjectResult.NotFound
        }
    }

    suspend fun list(ownerId: String): ListProjectsResult {
        val result =
            listProjectsOperation.execute(
                ListProjectsOperation.Arg(ownerId = ownerId),
            )
        return when (result) {
            is ListProjectsOperation.Result.Success ->
                ListProjectsResult.Success(
                    projects = result.projects.map { it.toData() },
                )
        }
    }

    suspend fun update(
        projectId: String,
        name: String?,
        description: String?,
    ): UpdateProjectResult {
        val result =
            updateProjectOperation.execute(
                UpdateProjectOperation.Arg(
                    projectId = projectId,
                    name = name,
                    description = description,
                ),
            )
        return when (result) {
            is UpdateProjectOperation.Result.Success -> {
                logAuditEventOperation.execute(
                    LogAuditEventOperation.Arg(
                        projectId = projectId,
                        documentId = null,
                        userId = result.project.ownerId.value,
                        action = "project.updated",
                        details = null,
                    ),
                )
                UpdateProjectResult.Success(
                    project = result.project.toData(),
                )
            }
            UpdateProjectOperation.Result.NotFound -> UpdateProjectResult.NotFound
        }
    }

    suspend fun delete(projectId: String): DeleteProjectResult {
        val result =
            deleteProjectOperation.execute(
                DeleteProjectOperation.Arg(projectId = projectId),
            )
        return when (result) {
            DeleteProjectOperation.Result.Success -> DeleteProjectResult.Success
            DeleteProjectOperation.Result.NotFound -> DeleteProjectResult.NotFound
        }
    }

    suspend fun listMembers(projectId: String): ListProjectMembersResult {
        val result =
            listProjectMembersOperation.execute(
                ListProjectMembersOperation.Arg(projectId = projectId),
            )
        return when (result) {
            is ListProjectMembersOperation.Result.Success ->
                ListProjectMembersResult.Success(
                    members = result.members.map { ProjectMemberDto(userId = it.userId, displayName = it.displayName, addedAt = it.addedAt) },
                )
        }
    }

    suspend fun addMember(projectId: String, userId: String, invitedBy: String): AddProjectMemberResult {
        val result =
            addProjectMemberOperation.execute(
                AddProjectMemberOperation.Arg(projectId = projectId, userId = userId),
            )
        return when (result) {
            AddProjectMemberOperation.Result.Success -> {
                logAuditEventOperation.execute(
                    LogAuditEventOperation.Arg(
                        projectId = projectId,
                        documentId = null,
                        userId = invitedBy,
                        action = "project.member.invited",
                        details = "{\"target_user_id\":\"${userId}\"}",
                    ),
                )
                AddProjectMemberResult.Success
            }
            AddProjectMemberOperation.Result.ProjectNotFound -> AddProjectMemberResult.ProjectNotFound
        }
    }

    suspend fun removeMember(projectId: String, userId: String, removedBy: String): RemoveProjectMemberResult {
        val result =
            removeProjectMemberOperation.execute(
                RemoveProjectMemberOperation.Arg(projectId = projectId, userId = userId),
            )
        return when (result) {
            RemoveProjectMemberOperation.Result.Success -> {
                logAuditEventOperation.execute(
                    LogAuditEventOperation.Arg(
                        projectId = projectId,
                        documentId = null,
                        userId = removedBy,
                        action = "project.member.removed",
                        details = "{\"target_user_id\":\"${userId}\"}",
                    ),
                )
                RemoveProjectMemberResult.Success
            }
            RemoveProjectMemberOperation.Result.ProjectNotFound -> RemoveProjectMemberResult.ProjectNotFound
        }
    }

    suspend fun listMemberProjects(userId: String): ListMemberProjectsResult {
        val result =
            listMemberProjectsOperation.execute(
                ListMemberProjectsOperation.Arg(userId = userId),
            )
        return when (result) {
            is ListMemberProjectsOperation.Result.Success ->
                ListMemberProjectsResult.Success(
                    projects = result.projects.map { it.toData() },
                )
        }
    }

    private fun Project.toData(): ProjectData =
        ProjectData(
            id = id.value,
            ownerId = ownerId.value,
            name = name,
            description = description,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )

    sealed interface CreateProjectResult {
        data class Success(
            val project: ProjectData,
        ) : CreateProjectResult

        data class Failure(
            val reason: String,
        ) : CreateProjectResult
    }

    sealed interface GetProjectResult {
        data class Success(
            val project: ProjectData,
        ) : GetProjectResult

        data object NotFound : GetProjectResult
    }

    sealed interface ListProjectsResult {
        data class Success(
            val projects: List<ProjectData>,
        ) : ListProjectsResult
    }

    sealed interface UpdateProjectResult {
        data class Success(
            val project: ProjectData,
        ) : UpdateProjectResult

        data object NotFound : UpdateProjectResult
    }

    sealed interface DeleteProjectResult {
        data object Success : DeleteProjectResult

        data object NotFound : DeleteProjectResult
    }

    sealed interface ListProjectMembersResult {
        data class Success(
            val members: List<ProjectMemberDto>,
        ) : ListProjectMembersResult
    }

    sealed interface AddProjectMemberResult {
        data object Success : AddProjectMemberResult
        data object ProjectNotFound : AddProjectMemberResult
    }

    sealed interface RemoveProjectMemberResult {
        data object Success : RemoveProjectMemberResult
        data object ProjectNotFound : RemoveProjectMemberResult
    }

    sealed interface ListMemberProjectsResult {
        data class Success(
            val projects: List<ProjectData>,
        ) : ListMemberProjectsResult
    }
}
