package com.kanban.http.access

import com.kanban.access.AccessHandler
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/groups/{id}/permissions")
internal class ListGroupPermissionsController(
    private val handler: AccessHandler,
) {
    @GetMapping
    suspend fun list(
        @PathVariable("id") id: String,
    ): ResponseEntity<*> {
        val request = AccessHandler.ListGroupPermissionsRequest(groupId = id)
        val result = handler.listGroupPermissions(request)
        return when (result) {
            is AccessHandler.ListGroupPermissionsResult.Success ->
                ResponseEntity.ok(mapOf("permissions" to result.permissions))
        }
    }
}
