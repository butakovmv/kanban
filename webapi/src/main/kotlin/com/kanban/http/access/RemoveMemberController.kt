package com.kanban.http.access

import com.kanban.access.AccessHandler
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/groups/{id}/members/{userId}")
internal class RemoveMemberController(
    private val handler: AccessHandler,
) {
    @DeleteMapping
    suspend fun remove(
        @PathVariable("id") id: String,
        @PathVariable("userId") userId: String,
    ): ResponseEntity<*> {
        val request =
            AccessHandler.RemoveMemberRequest(
                groupId = id,
                userId = userId,
            )
        val result = handler.removeMember(request)
        return when (result) {
            AccessHandler.RemoveMemberResult.Success ->
                ResponseEntity
                    .status(HttpStatus.NO_CONTENT)
                    .build<Any>()
            is AccessHandler.RemoveMemberResult.Failure ->
                ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(mapOf("reason" to result.reason))
        }
    }
}
