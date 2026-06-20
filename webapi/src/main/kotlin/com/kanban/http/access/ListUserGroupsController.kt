package com.kanban.http.access

import com.kanban.access.AccessHandler
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/users/{id}/groups")
internal class ListUserGroupsController(
    private val handler: AccessHandler,
) {
    @GetMapping
    suspend fun list(
        @PathVariable("id") id: String,
    ): ResponseEntity<*> {
        val request = AccessHandler.ListUserGroupsRequest(userId = id)
        val result = handler.listUserGroups(request)
        return when (result) {
            is AccessHandler.ListUserGroupsResult.Success ->
                ResponseEntity.ok(mapOf("groups" to result.groups))
        }
    }
}
