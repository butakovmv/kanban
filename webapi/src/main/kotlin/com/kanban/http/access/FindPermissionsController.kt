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
        val request =
            AccessHandler.FindPermissionsRequest(
                resource = resource,
                targetId = targetId,
            )
        val result = handler.findPermissions(request)
        return when (result) {
            is AccessHandler.FindPermissionsResult.Success ->
                ResponseEntity.ok(mapOf("permissions" to result.permissions))
        }
    }
}
