package com.kanban.http.auth

import com.kanban.identity.RecoveryHandler
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth/recovery/request")
internal class RecoveryRequestController(
    private val handler: RecoveryHandler,
) {
    @PostMapping
    suspend fun requestRecovery(
        @RequestBody body: RecoveryRequestBody,
    ): ResponseEntity<*> {
        val result = handler.requestRecovery(email = body.email)
        return when (result) {
            is RecoveryHandler.RecoveryResult.Success ->
                ResponseEntity.ok(mapOf("message" to result.message))
            is RecoveryHandler.RecoveryResult.Failure ->
                ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(mapOf("reason" to result.reason))
        }
    }
}

data class RecoveryRequestBody(
    val email: String,
)
