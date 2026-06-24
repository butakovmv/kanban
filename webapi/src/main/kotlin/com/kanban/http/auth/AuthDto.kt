package com.kanban.http.auth

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Authentication response with tokens and user info")
data class AuthResponse(
    @JsonProperty("access_token")
    @field:Schema(description = "JWT access token")
    val accessToken: String,
    @JsonProperty("refresh_token")
    @field:Schema(description = "JWT refresh token")
    val refreshToken: String,
    @field:Schema(description = "User information")
    val user: UserResponse,
)

@Schema(description = "Token refresh response")
data class TokenResponse(
    @JsonProperty("access_token")
    @field:Schema(description = "JWT access token")
    val accessToken: String,
    @JsonProperty("refresh_token")
    @field:Schema(description = "JWT refresh token")
    val refreshToken: String,
)

@Schema(description = "User information")
data class UserResponse(
    @field:Schema(description = "User unique identifier", example = "550e8400-e29b-41d4-a716-446655440000")
    val id: String,
    @field:Schema(description = "User email", example = "user@example.com")
    val email: String,
    @JsonProperty("display_name")
    @field:Schema(description = "User display name", example = "John Doe")
    val displayName: String,
)
