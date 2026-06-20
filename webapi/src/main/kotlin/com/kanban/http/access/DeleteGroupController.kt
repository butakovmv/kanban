package com.kanban.http.access

import com.kanban.access.AccessHandler
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/groups/{id}")
internal class DeleteGroupController(
    private val handler: AccessHandler,
) {
    @DeleteMapping
    suspend fun delete(
        @PathVariable("id") id: String,
    ): ResponseEntity<*> {
        val request = AccessHandler.DeleteGroupRequest(groupId = id)
        val result = handler.deleteGroup(request)
        return when (result) {
            AccessHandler.DeleteGroupResult.Success ->
                ResponseEntity
                    .status(HttpStatus.NO_CONTENT)
                    .build<Any>()
            AccessHandler.DeleteGroupResult.NotFound ->
                ResponseEntity.notFound().build<Any>()
        }
    }
}
