package com.kanban.http.access

import com.kanban.access.AccessHandler
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/groups")
internal class CreateGroupController(
    private val handler: AccessHandler,
) {
    @PostMapping
    suspend fun create(
        @RequestBody body: CreateGroupBody,
    ): ResponseEntity<*> {
        val result = handler.createGroup(name = body.name, description = body.description)
        return when (result) {
            is AccessHandler.CreateGroupResult.Success ->
                ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(
                        GroupResponse(
                            id = result.group.id,
                            name = result.group.name,
                            description = result.group.description,
                            createdAt = result.group.createdAt,
                        ),
                    )
            is AccessHandler.CreateGroupResult.Failure ->
                ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(mapOf("reason" to result.reason))
        }
    }

    data class CreateGroupBody(
        val name: String,
        val description: String?,
    )
}
