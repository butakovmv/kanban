package com.kanban.http.project

import com.kanban.project.ProjectHandler
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/projects/{id}/members")
internal class RemoveProjectMemberController(
    private val handler: ProjectHandler,
) {
    @DeleteMapping
    suspend fun removeMember(
        @PathVariable("id") projectId: String,
        @RequestParam("user_id") userId: String,
    ): ResponseEntity<*> {
        val result = handler.removeMember(projectId = projectId, userId = userId)
        return when (result) {
            ProjectHandler.RemoveProjectMemberResult.Success ->
                ResponseEntity.ok(mapOf("status" to "ok"))
            ProjectHandler.RemoveProjectMemberResult.ProjectNotFound ->
                ResponseEntity.notFound().build<Any>()
        }
    }
}
