package com.kanban.http.project

import com.fasterxml.jackson.annotation.JsonProperty
import com.kanban.project.ProjectHandler
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/projects/{id}/members")
internal class AddProjectMemberController(
    private val handler: ProjectHandler,
) {
    data class AddMemberBody(
        @JsonProperty("user_id")
        val userId: String,
        @JsonProperty("invited_by")
        val invitedBy: String,
    )

    @PostMapping
    suspend fun addMember(
        @PathVariable("id") projectId: String,
        @RequestBody body: AddMemberBody,
    ): ResponseEntity<*> {
        val result = handler.addMember(projectId = projectId, userId = body.userId, invitedBy = body.invitedBy)
        return when (result) {
            ProjectHandler.AddProjectMemberResult.Success ->
                ResponseEntity.ok(mapOf("status" to "ok"))
            ProjectHandler.AddProjectMemberResult.ProjectNotFound ->
                ResponseEntity.notFound().build<Any>()
        }
    }
}
