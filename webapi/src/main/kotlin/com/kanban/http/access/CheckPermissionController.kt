package com.kanban.http.access

import com.kanban.access.AccessHandler
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/permissions/check")
internal class CheckPermissionController(
    private val handler: AccessHandler,
) {
    @GetMapping
    suspend fun check(
        @RequestParam("user_id") userId: String,
        @RequestParam resource: String,
        @RequestParam action: String,
        @RequestParam("target_id") targetId: String?,
    ): ResponseEntity<*> {
        val result = handler.checkPermission(userId = userId, resource = resource, action = action, targetId = targetId)
        return when (result) {
            is AccessHandler.CheckPermissionResult.Success ->
                ResponseEntity.ok(CheckPermissionResponse(allowed = result.allowed, reason = result.reason))
        }
    }
}
