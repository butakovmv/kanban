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
@RequestMapping("/api/v1/auth/register")
internal class RegisterController(
    private val handler: AuthHandler,
) {
    @PostMapping
    suspend fun register(
        @RequestBody body: RegisterBody,
    ): ResponseEntity<*> {
        val result = handler.register(
            email = body.email,
            password = body.password,
            displayName = body.displayName,
        )
        return when (result) {
            is AuthHandler.AuthResult.Success ->
                ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(
                        AuthResponse(
                            accessToken = result.accessToken,
                            refreshToken = result.refreshToken,
                            user = UserResponse(
                                id = result.userId,
                                email = result.email,
                                displayName = result.displayName,
                            ),
                        ),
                    )
            is AuthHandler.AuthResult.Failure ->
                ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(mapOf("reason" to result.reason))
        }
    }
}

data class RegisterBody(
    val email: String,
    val password: String,
    @JsonProperty("display_name")
    val displayName: String,
)
