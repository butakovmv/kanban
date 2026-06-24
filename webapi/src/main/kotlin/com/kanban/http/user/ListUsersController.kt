package com.kanban.http.user

import com.fasterxml.jackson.annotation.JsonProperty
import com.kanban.identity.UserDisplayInfo
import com.kanban.identity.UserHandler
import org.springframework.http.CacheControl
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/users")
internal class ListUsersController(
    private val handler: UserHandler,
) {
    @GetMapping
    suspend fun list(
        @RequestParam("ids") ids: String,
    ): ResponseEntity<*> {
        val userIds = ids.split(",").map { it.trim() }.filter { it.isNotBlank() }
        val users = handler.findUsers(userIds)
        return ResponseEntity.ok()
            .cacheControl(CacheControl.maxAge(java.time.Duration.ofHours(2)))
            .body(mapOf("users" to users.map { it.toResponse() }))
    }
}

internal data class UserDisplayResponse(
    val id: String,
    @JsonProperty("display_name")
    val displayName: String,
)

internal fun UserDisplayInfo.toResponse() = UserDisplayResponse(id = id, displayName = displayName)
