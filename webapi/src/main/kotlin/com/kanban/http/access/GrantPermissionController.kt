package com.kanban.http.access

import com.fasterxml.jackson.annotation.JsonProperty
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
        @RequestBody body: GrantPermissionBody,
    ): ResponseEntity<*> {
        val result = handler.grantPermission(groupId = id, permissionId = body.permissionId)
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

    data class GrantPermissionBody(
        @JsonProperty("permission_id")
        val permissionId: String,
    )
}
