package com.kanban.http.project

import com.kanban.project.ProjectHandler
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/projects")
internal class ListProjectsController(
    private val handler: ProjectHandler,
) {
    @GetMapping
    suspend fun list(@RequestParam("owner_id") ownerId: String): ResponseEntity<*> {
        val result = handler.list(ownerId = ownerId)
        return when (result) {
            is ProjectHandler.ListProjectsResult.Success ->
                ResponseEntity.ok(
                    mapOf(
                        "projects" to result.projects.map { p ->
                            ProjectResponse(
                                id = p.id, ownerId = p.ownerId, name = p.name,
                                description = p.description, createdAt = p.createdAt, updatedAt = p.updatedAt,
                            )
                        },
                    ),
                )
        }
    }
}
