package com.kanban.http.auth

import com.fasterxml.jackson.annotation.JsonProperty
import com.kanban.identity.AuthHandler
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth/logout")
internal class LogoutController(
    private val handler: AuthHandler,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun logout(
        @RequestBody body: LogoutBody,
    ) {
        handler.logout(refreshToken = body.refreshToken)
    }
}

data class LogoutBody(
    @JsonProperty("refresh_token")
    val refreshToken: String?,
)
