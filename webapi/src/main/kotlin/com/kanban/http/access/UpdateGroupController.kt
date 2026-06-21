package com.kanban.http.access

import com.kanban.access.AccessHandler
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/groups/{id}")
internal class UpdateGroupController(
    private val handler: AccessHandler,
) {
    @PutMapping
    suspend fun update(
        @PathVariable("id") id: String,
        @RequestBody body: UpdateGroupBody,
    ): ResponseEntity<*> {
        val result = handler.updateGroup(groupId = id, name = body.name, description = body.description)
        return when (result) {
            is AccessHandler.UpdateGroupResult.Success ->
                ResponseEntity.ok(GroupResponse(id = result.group.id, name = result.group.name, description = result.group.description, createdAt = result.group.createdAt))
            AccessHandler.UpdateGroupResult.NotFound ->
                ResponseEntity.notFound().build<Any>()
            is AccessHandler.UpdateGroupResult.Failure ->
                ResponseEntity.badRequest().body(mapOf("reason" to result.reason))
        }
    }

    data class UpdateGroupBody(
        val name: String?,
        val description: String?,
    )
}
