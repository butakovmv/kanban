package com.kanban.http.access

import com.kanban.access.AccessHandler
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/groups/{id}")
internal class GetGroupController(
    private val handler: AccessHandler,
) {
    @GetMapping
    suspend fun get(
        @PathVariable("id") id: String,
    ): ResponseEntity<*> {
        val request = AccessHandler.GetGroupRequest(groupId = id)
        val result = handler.getGroup(request)
        return when (result) {
            is AccessHandler.GetGroupResult.Success ->
                ResponseEntity.ok(result.group)
            AccessHandler.GetGroupResult.NotFound ->
                ResponseEntity.notFound().build<Any>()
        }
    }
}
