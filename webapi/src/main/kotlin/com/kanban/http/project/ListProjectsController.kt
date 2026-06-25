package com.kanban.http.project

import com.kanban.project.ProjectHandler
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/projects")
@Tag(name = "Projects", description = "Project management operations")
internal class ListProjectsController(
    private val handler: ProjectHandler,
) {
    @Operation(summary = "List projects by owner")
    @ApiResponse(
        responseCode = "200",
        description = "List of projects",
        content = [Content(schema = Schema(implementation = ProjectsListResponse::class))],
    )
    @GetMapping
    suspend fun list(
        @Parameter(description = "Owner user ID") @RequestParam("owner_id") ownerId: String,
    ): ResponseEntity<*> {
        val result = handler.list(ownerId = ownerId)
        return when (result) {
            is ProjectHandler.ListProjectsResult.Success ->
                ResponseEntity.ok(
                    mapOf(
                        "projects" to
                            result.projects.map { p ->
                                ProjectResponse(
                                    id = p.id,
                                    ownerId = p.ownerId,
                                    name = p.name,
                                    description = p.description,
                                    createdAt = p.createdAt,
                                    updatedAt = p.updatedAt,
                                )
                            },
                    ),
                )
        }
    }
}

@Schema(description = "List of projects response")
data class ProjectsListResponse(
    @field:Schema(description = "List of projects")
    val projects: List<ProjectResponse>,
)
