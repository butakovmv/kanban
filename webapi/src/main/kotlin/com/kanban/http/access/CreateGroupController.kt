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
        @RequestBody request: AccessHandler.CreateGroupRequest,
    ): ResponseEntity<*> {
        val result = handler.createGroup(request)
        return when (result) {
            is AccessHandler.CreateGroupResult.Success ->
                ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(result.group)
            is AccessHandler.CreateGroupResult.Failure ->
                ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(mapOf("reason" to result.reason))
        }
    }
}
