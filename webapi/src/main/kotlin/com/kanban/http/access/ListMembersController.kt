package com.kanban.http.access

import com.kanban.access.AccessHandler
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/groups/{id}/members")
internal class ListMembersController(
    private val handler: AccessHandler,
) {
    @GetMapping
    suspend fun list(
        @PathVariable("id") id: String,
    ): ResponseEntity<*> {
        val result = handler.listMembers(groupId = id)
        return when (result) {
            is AccessHandler.ListMembersResult.Success ->
                ResponseEntity.ok(
                    mapOf(
                        "members" to
                            result.members.map {
                                MemberResponse(groupId = it.groupId, userId = it.userId, addedAt = it.addedAt)
                            },
                    ),
                )
        }
    }
}
