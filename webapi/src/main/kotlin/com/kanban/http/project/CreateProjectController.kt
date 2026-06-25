package com.kanban.http.project

import com.fasterxml.jackson.annotation.JsonProperty
import com.kanban.http.ErrorResponse
import com.kanban.project.ProjectHandler
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/projects")
@Tag(name = "Projects", description = "Project management operations")
internal class CreateProjectController(
    private val handler: ProjectHandler,
) {
    data class CreateProjectBody(
        @JsonProperty("owner_id")
        @field:Schema(description = "Owner user ID", example = "550e8400-e29b-41d4-a716-446655440000")
        val ownerId: String,
        @field:Schema(description = "Project name", example = "My Project")
        val name: String,
        @field:Schema(description = "Project description", example = "Project description", nullable = true)
        val description: String?,
    )

    @Operation(summary = "Create a new project")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "Project created successfully",
                content = [Content(schema = Schema(implementation = ProjectResponse::class))],
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid request body or validation error",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
        ],
    )
    @PostMapping
    suspend fun create(
        @RequestBody body: CreateProjectBody,
    ): ResponseEntity<*> {
        val result =
            handler.create(
                ownerId = body.ownerId,
                name = body.name,
                description = body.description,
            )
        return when (result) {
            is ProjectHandler.CreateProjectResult.Success -> {
                val p = result.project
                ResponseEntity.status(HttpStatus.CREATED).body(
                    ProjectResponse(
                        id = p.id,
                        ownerId = p.ownerId,
                        name = p.name,
                        description = p.description,
                        createdAt = p.createdAt,
                        updatedAt = p.updatedAt,
                    ),
                )
            }
            is ProjectHandler.CreateProjectResult.Failure ->
                ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("reason" to result.reason))
        }
    }
}
