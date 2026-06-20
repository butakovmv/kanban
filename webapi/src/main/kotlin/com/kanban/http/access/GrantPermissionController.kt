package com.kanban.http.access

import com.kanban.access.AccessHandler
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/groups/{id}/permissions")
internal class GrantPermissionController(
    private val handler: AccessHandler,
) {
    @PostMapping
    suspend fun grant(
        @PathVariable("id") id: String,
        @RequestBody body: AccessHandler.GrantPermissionRequest,
    ): ResponseEntity<*> {
        val request =
            AccessHandler.GrantPermissionRequest(
                groupId = id,
                permissionId = body.permissionId,
            )
        val result = handler.grantPermission(request)
        return when (result) {
            AccessHandler.GrantPermissionResult.Success ->
                ResponseEntity
                    .status(HttpStatus.CREATED)
                    .build<Any>()
            is AccessHandler.GrantPermissionResult.Failure ->
                ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(mapOf("reason" to result.reason))
        }
    }
}
