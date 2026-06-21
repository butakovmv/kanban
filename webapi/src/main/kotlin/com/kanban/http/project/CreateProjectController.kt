package com.kanban.http.project

import com.fasterxml.jackson.annotation.JsonProperty
import com.kanban.project.ProjectHandler
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/projects")
internal class CreateProjectController(
    private val handler: ProjectHandler,
) {
    data class CreateProjectBody(
        @JsonProperty("owner_id")
        val ownerId: String,
        val name: String,
        val description: String?,
    )

    @PostMapping
    suspend fun create(
        @RequestBody body: CreateProjectBody,
    ): ResponseEntity<*> {
        val result = handler.create(
            ownerId = body.ownerId,
            name = body.name,
            description = body.description,
        )
        return when (result) {
            is ProjectHandler.CreateProjectResult.Success -> {
                val p = result.project
                ResponseEntity.status(HttpStatus.CREATED).body(
                    ProjectResponse(
                        id = p.id, ownerId = p.ownerId, name = p.name,
                        description = p.description, createdAt = p.createdAt, updatedAt = p.updatedAt,
                    ),
                )
            }
            is ProjectHandler.CreateProjectResult.Failure ->
                ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("reason" to result.reason))
        }
    }
}
