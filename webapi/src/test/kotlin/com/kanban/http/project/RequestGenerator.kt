package com.kanban.http.project

import java.util.UUID

internal object RequestGenerator {
    fun createProjectRequest(): CreateProjectController.CreateProjectBody =
        CreateProjectController.CreateProjectBody(
            ownerId = "owner-${UUID.randomUUID()}",
            name = "Project ${UUID.randomUUID().toString().take(6)}",
            description = "Description ${UUID.randomUUID().toString().take(6)}",
        )

    fun createProjectRequestWithoutDescription(): CreateProjectController.CreateProjectBody =
        CreateProjectController.CreateProjectBody(
            ownerId = "owner-${UUID.randomUUID()}",
            name = "Project ${UUID.randomUUID().toString().take(6)}",
            description = null,
        )

    fun updateProjectBody(): UpdateProjectController.UpdateProjectBody =
        UpdateProjectController.UpdateProjectBody(
            name = "Updated ${UUID.randomUUID().toString().take(6)}",
            description = "Updated ${UUID.randomUUID().toString().take(6)}",
        )

    fun createBoardRequest(): CreateBoardController.CreateBoardBody =
        CreateBoardController.CreateBoardBody(
            projectId = "project-${UUID.randomUUID()}",
            name = "Board ${UUID.randomUUID().toString().take(6)}",
        )

    fun updateBoardBody(): UpdateBoardController.UpdateBoardBody =
        UpdateBoardController.UpdateBoardBody(
            name = "Updated ${UUID.randomUUID().toString().take(6)}",
        )

    fun reorderColumnsBody(): ReorderColumnsController.ReorderColumnsBody =
        ReorderColumnsController.ReorderColumnsBody(
            columnIds =
                listOf(
                    "col-${UUID.randomUUID()}",
                    "col-${UUID.randomUUID()}",
                    "col-${UUID.randomUUID()}",
                ),
        )
}
