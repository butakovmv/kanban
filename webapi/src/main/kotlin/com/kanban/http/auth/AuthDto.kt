package com.kanban.http.auth

import com.fasterxml.jackson.annotation.JsonProperty

data class AuthResponse(
    @JsonProperty("access_token")
    val accessToken: String,
    @JsonProperty("refresh_token")
    val refreshToken: String,
    val user: UserResponse,
)

data class TokenResponse(
    @JsonProperty("access_token")
    val accessToken: String,
    @JsonProperty("refresh_token")
    val refreshToken: String,
)

data class UserResponse(
    val id: String,
    val email: String,
    @JsonProperty("display_name")
    val displayName: String,
)
