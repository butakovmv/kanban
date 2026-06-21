package com.kanban.http.access

import com.kanban.access.AccessHandler
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/permissions/{id}")
internal class DeletePermissionController(
    private val handler: AccessHandler,
) {
    @DeleteMapping
    suspend fun delete(
        @PathVariable("id") id: String,
    ): ResponseEntity<*> {
        val result = handler.deletePermission(permissionId = id)
        return when (result) {
            AccessHandler.DeletePermissionResult.Success ->
                ResponseEntity
                    .status(HttpStatus.NO_CONTENT)
                    .build<Any>()
            AccessHandler.DeletePermissionResult.NotFound ->
                ResponseEntity.notFound().build<Any>()
        }
    }
}
