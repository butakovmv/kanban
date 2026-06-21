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
        val result = handler.getGroup(groupId = id)
        return when (result) {
            is AccessHandler.GetGroupResult.Success ->
                ResponseEntity.ok(GroupResponse(id = result.group.id, name = result.group.name, description = result.group.description, createdAt = result.group.createdAt))
            AccessHandler.GetGroupResult.NotFound ->
                ResponseEntity.notFound().build<Any>()
        }
    }
}
