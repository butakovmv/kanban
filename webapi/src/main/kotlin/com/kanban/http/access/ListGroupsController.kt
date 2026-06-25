package com.kanban.http.access

import com.kanban.access.AccessHandler
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/groups")
internal class ListGroupsController(
    private val handler: AccessHandler,
) {
    @GetMapping
    suspend fun list(): ResponseEntity<*> {
        val result = handler.listGroups()
        return when (result) {
            is AccessHandler.ListGroupsResult.Success ->
                ResponseEntity.ok(
                    mapOf(
                        "groups" to
                            result.groups.map {
                                GroupResponse(id = it.id, name = it.name, description = it.description, createdAt = it.createdAt)
                            },
                    ),
                )
        }
    }
}
