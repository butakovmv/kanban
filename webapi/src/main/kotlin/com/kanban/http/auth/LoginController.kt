package com.kanban.http.auth

import com.kanban.identity.AuthHandler
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth/login")
internal class LoginController(
    private val handler: AuthHandler,
) {
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
                        user = UserResponse(
                            id = result.userId,
                            email = result.email,
                            displayName = result.displayName,
                        ),
                    ),
                )
            is AuthHandler.AuthResult.Failure ->
                ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(mapOf("reason" to result.reason))
        }
    }
}

data class LoginBody(
    val email: String,
    val password: String,
)
