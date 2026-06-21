package com.kanban.http.access

import com.fasterxml.jackson.annotation.JsonProperty
import com.kanban.access.AccessHandler
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/groups/{id}/members")
internal class AddMemberController(
    private val handler: AccessHandler,
) {
    @PostMapping
    suspend fun add(
        @PathVariable("id") id: String,
        @RequestBody body: AddMemberBody,
    ): ResponseEntity<*> {
        val result = handler.addMember(groupId = id, userId = body.userId)
        return when (result) {
            AccessHandler.AddMemberResult.Success ->
                ResponseEntity
                    .status(HttpStatus.CREATED)
                    .build<Any>()
            is AccessHandler.AddMemberResult.Failure ->
                ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(mapOf("reason" to result.reason))
        }
    }

    data class AddMemberBody(
        @JsonProperty("user_id")
        val userId: String,
    )
}
