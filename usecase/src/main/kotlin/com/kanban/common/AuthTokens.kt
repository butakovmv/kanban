package com.kanban.common

data class AuthTokens(
    val accessToken: AccessToken,
    val refreshToken: RefreshToken,
)
