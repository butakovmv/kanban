package com.kanban.http.access

import com.kanban.access.AccessHandler
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/permissions")
internal class FindPermissionsController(
    private val handler: AccessHandler,
) {
    @GetMapping
    suspend fun find(
        @RequestParam resource: String,
        @RequestParam("target_id") targetId: String?,
    ): ResponseEntity<*> {
        val result = handler.findPermissions(resource = resource, targetId = targetId)
        return when (result) {
            is AccessHandler.FindPermissionsResult.Success ->
                ResponseEntity.ok(
                    mapOf(
                        "permissions" to
                            result.permissions.map {
                                PermissionResponse(
                                    id = it.id,
                                    resource = it.resource,
                                    action = it.action,
                                    targetId = it.targetId,
                                    createdAt = it.createdAt,
                                )
                            },
                    ),
                )
        }
    }
}
