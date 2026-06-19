package com.kanban.http.project

import com.kanban.project.BoardHandler
import com.kanban.project.ProjectHandler
import java.util.UUID

/**
 * Генератор тестовых DTO для запросов проектов и досок.
 * Создаёт случайные данные, которые используются в тестах контроллеров.
 */
internal object RequestGenerator {
    fun createProjectRequest(): ProjectHandler.CreateProjectRequest =
        ProjectHandler.CreateProjectRequest(
            ownerId = "owner-${UUID.randomUUID()}",
            name = "Project ${UUID.randomUUID().toString().take(6)}",
            description = "Description ${UUID.randomUUID().toString().take(6)}",
        )

    fun createProjectRequestWithoutDescription(): ProjectHandler.CreateProjectRequest =
        ProjectHandler.CreateProjectRequest(
            ownerId = "owner-${UUID.randomUUID()}",
            name = "Project ${UUID.randomUUID().toString().take(6)}",
            description = null,
        )

    fun listProjectsRequest(): ProjectHandler.ListProjectsRequest =
        ProjectHandler.ListProjectsRequest(
            ownerId = "owner-${UUID.randomUUID()}",
        )

    fun updateProjectBody(): ProjectHandler.UpdateProjectBody =
        ProjectHandler.UpdateProjectBody(
            name = "Updated ${UUID.randomUUID().toString().take(6)}",
            description = "Updated ${UUID.randomUUID().toString().take(6)}",
        )

    fun createBoardRequest(): BoardHandler.CreateBoardRequest =
        BoardHandler.CreateBoardRequest(
            projectId = "project-${UUID.randomUUID()}",
            name = "Board ${UUID.randomUUID().toString().take(6)}",
        )

    fun updateBoardBody(): BoardHandler.UpdateBoardBody =
        BoardHandler.UpdateBoardBody(
            name = "Updated ${UUID.randomUUID().toString().take(6)}",
        )

    fun reorderColumnsBody(): BoardHandler.ReorderColumnsBody =
        BoardHandler.ReorderColumnsBody(
            columnIds =
                listOf(
                    "col-${UUID.randomUUID()}",
                    "col-${UUID.randomUUID()}",
                    "col-${UUID.randomUUID()}",
                ),
        )
}
