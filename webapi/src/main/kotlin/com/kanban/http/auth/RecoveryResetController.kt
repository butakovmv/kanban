package com.kanban.http.auth

import com.fasterxml.jackson.annotation.JsonProperty
import com.kanban.identity.RecoveryHandler
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth/recovery/reset")
internal class RecoveryResetController(
    private val handler: RecoveryHandler,
) {
    @PostMapping
    suspend fun resetPassword(
        @RequestBody body: ResetPasswordBody,
    ): ResponseEntity<*> {
        val result = handler.resetPassword(token = body.token, newPassword = body.newPassword)
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

data class ResetPasswordBody(
    val token: String,
    @JsonProperty("new_password")
    val newPassword: String,
)
