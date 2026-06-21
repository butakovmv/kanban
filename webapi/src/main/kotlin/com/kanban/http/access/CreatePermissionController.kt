package com.kanban.http.access

import com.fasterxml.jackson.annotation.JsonProperty
import com.kanban.access.AccessHandler
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/permissions")
internal class CreatePermissionController(
    private val handler: AccessHandler,
) {
    @PostMapping
    suspend fun create(
        @RequestBody body: CreatePermissionBody,
    ): ResponseEntity<*> {
        val result = handler.createPermission(resource = body.resource, action = body.action, targetId = body.targetId)
        return when (result) {
            is AccessHandler.CreatePermissionResult.Success ->
                ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(PermissionResponse(id = result.permission.id, resource = result.permission.resource, action = result.permission.action, targetId = result.permission.targetId, createdAt = result.permission.createdAt))
            is AccessHandler.CreatePermissionResult.Failure ->
                ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(mapOf("reason" to result.reason))
        }
    }

    data class CreatePermissionBody(
        val resource: String,
        val action: String,
        @JsonProperty("target_id")
        val targetId: String?,
    )
}
