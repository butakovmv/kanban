package com.kanban.http.project

import com.kanban.project.ProjectHandler
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/projects/{id}")
internal class GetProjectController(
    private val handler: ProjectHandler,
) {
    @GetMapping
    suspend fun get(
        @PathVariable("id") id: String,
    ): ResponseEntity<*> {
        val result = handler.get(projectId = id)
        return when (result) {
            is ProjectHandler.GetProjectResult.Success -> {
                val p = result.project
                ResponseEntity.ok(
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
            ProjectHandler.GetProjectResult.NotFound ->
                ResponseEntity.notFound().build<Any>()
        }
    }
}
