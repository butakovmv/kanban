package com.kanban.http.auth

import com.fasterxml.jackson.annotation.JsonProperty
import com.kanban.identity.AuthHandler
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth/refresh")
internal class RefreshController(
    private val handler: AuthHandler,
) {
    @PostMapping
    suspend fun refresh(
        @RequestBody body: RefreshBody,
    ): ResponseEntity<*> {
        val result = handler.refresh(refreshToken = body.refreshToken)
        return when (result) {
            is AuthHandler.TokenResult.Success ->
                ResponseEntity.ok(
                    TokenResponse(
                        accessToken = result.accessToken,
                        refreshToken = result.refreshToken,
                    ),
                )
            is AuthHandler.TokenResult.Failure ->
                ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(mapOf("reason" to result.reason))
        }
    }
}

data class RefreshBody(
    @JsonProperty("refresh_token")
    val refreshToken: String,
)
