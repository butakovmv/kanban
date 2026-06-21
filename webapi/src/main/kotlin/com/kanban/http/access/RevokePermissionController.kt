package com.kanban.http.access

import com.kanban.access.AccessHandler
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/groups/{id}/permissions/{permId}")
internal class RevokePermissionController(
    private val handler: AccessHandler,
) {
    @DeleteMapping
    suspend fun revoke(
        @PathVariable("id") id: String,
        @PathVariable("permId") permId: String,
    ): ResponseEntity<*> {
        val result = handler.revokePermission(groupId = id, permissionId = permId)
        return when (result) {
            AccessHandler.RevokePermissionResult.Success ->
                ResponseEntity
                    .status(HttpStatus.NO_CONTENT)
                    .build<Any>()
            is AccessHandler.RevokePermissionResult.Failure ->
                ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(mapOf("reason" to result.reason))
        }
    }
}
