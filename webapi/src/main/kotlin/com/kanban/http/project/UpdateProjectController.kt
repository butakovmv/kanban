package com.kanban.http.project

import com.kanban.project.ProjectHandler
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/projects/{id}")
internal class UpdateProjectController(
    private val handler: ProjectHandler,
) {
    data class UpdateProjectBody(
        val name: String?,
        val description: String?,
    )

    @PutMapping
    suspend fun update(
        @PathVariable("id") id: String,
        @RequestBody body: UpdateProjectBody,
    ): ResponseEntity<*> {
        val result = handler.update(
            projectId = id,
            name = body.name,
            description = body.description,
        )
        return when (result) {
            is ProjectHandler.UpdateProjectResult.Success -> {
                val p = result.project
                ResponseEntity.ok(
                    ProjectResponse(
                        id = p.id, ownerId = p.ownerId, name = p.name,
                        description = p.description, createdAt = p.createdAt, updatedAt = p.updatedAt,
                    ),
                )
            }
            ProjectHandler.UpdateProjectResult.NotFound ->
                ResponseEntity.notFound().build<Any>()
        }
    }
}
