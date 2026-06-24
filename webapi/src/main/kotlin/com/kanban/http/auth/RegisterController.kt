package com.kanban.http.auth

import com.fasterxml.jackson.annotation.JsonProperty
import com.kanban.http.ErrorResponse
import com.kanban.identity.AuthHandler
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth/register")
@Tag(name = "Authentication", description = "User authentication operations")
internal class RegisterController(
    private val handler: AuthHandler,
) {
    @Operation(
        summary = "Register",
        description = "Register a new user account",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "Registration successful",
                content = [Content(schema = Schema(implementation = AuthResponse::class))],
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid request body or email already exists",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
        ],
    )
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
    @field:Schema(description = "User email", example = "user@example.com")
    val email: String,
    @field:Schema(description = "User password", example = "password123")
    val password: String,
    @JsonProperty("display_name")
    @field:Schema(description = "Display name", example = "John Doe")
    val displayName: String,
)
