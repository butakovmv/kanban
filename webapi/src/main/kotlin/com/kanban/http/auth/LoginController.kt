package com.kanban.http.auth

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
@RequestMapping("/api/v1/auth/login")
@Tag(name = "Authentication", description = "User authentication operations")
internal class LoginController(
    private val handler: AuthHandler,
) {
    @Operation(
        summary = "Login",
        description = "Authenticate user with email and password",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Login successful",
                content = [Content(schema = Schema(implementation = AuthResponse::class))],
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid request body or missing fields",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
            ApiResponse(
                responseCode = "401",
                description = "Invalid credentials",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
        ],
    )
    @PostMapping
    suspend fun login(
        @RequestBody body: LoginBody,
    ): ResponseEntity<*> {
        val result = handler.login(email = body.email, password = body.password)
        return when (result) {
            is AuthHandler.AuthResult.Success ->
                ResponseEntity.ok(
                    AuthResponse(
                        accessToken = result.accessToken,
                        refreshToken = result.refreshToken,
                        user =
                            UserResponse(
                                id = result.userId,
                                email = result.email,
                                displayName = result.displayName,
                            ),
                    ),
                )
            is AuthHandler.AuthResult.Failure ->
                ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(mapOf("message" to result.reason, "reason" to result.reason))
        }
    }
}

data class LoginBody(
    @field:io.swagger.v3.oas.annotations.media.Schema(description = "User email", example = "user@example.com")
    val email: String,
    @field:io.swagger.v3.oas.annotations.media.Schema(description = "User password", example = "password123")
    val password: String,
)
