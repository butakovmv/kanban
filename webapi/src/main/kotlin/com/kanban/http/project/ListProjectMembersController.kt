package com.kanban.http.project

import com.fasterxml.jackson.annotation.JsonProperty
import com.kanban.project.ProjectHandler
import java.time.Instant
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/projects/{id}/members")
internal class ListProjectMembersController(
    private val handler: ProjectHandler,
) {
    data class MemberResponse(
        @JsonProperty("user_id")
        val userId: String,
        @JsonProperty("display_name")
        val displayName: String,
        @JsonProperty("added_at")
        val addedAt: Instant,
    )

    @GetMapping
    suspend fun listMembers(@PathVariable("id") projectId: String): ResponseEntity<*> {
        val result = handler.listMembers(projectId = projectId)
        return when (result) {
            is ProjectHandler.ListProjectMembersResult.Success ->
                ResponseEntity.ok(
                    mapOf(
                        "members" to result.members.map { MemberResponse(userId = it.userId, displayName = it.displayName, addedAt = it.addedAt) },
                    ),
                )
        }
    }
}
