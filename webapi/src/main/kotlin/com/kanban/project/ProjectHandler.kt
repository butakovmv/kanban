package com.kanban.project

import java.time.Instant

internal class ProjectHandler(
    private val createProjectOperation: CreateProjectOperation,
    private val getProjectOperation: GetProjectOperation,
    private val listProjectsOperation: ListProjectsOperation,
    private val updateProjectOperation: UpdateProjectOperation,
    private val deleteProjectOperation: DeleteProjectOperation,
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
            is CreateProjectOperation.Result.Success ->
                CreateProjectResult.Success(
                    project = result.project.toData(),
                )
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
            is UpdateProjectOperation.Result.Success ->
                UpdateProjectResult.Success(
                    project = result.project.toData(),
                )
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
}
