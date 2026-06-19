package com.kanban.identity

import com.kanban.common.AuthTokens

interface TokenProvider {
    suspend fun generateTokens(userId: String): AuthTokens

    suspend fun refreshAccessToken(refreshToken: String): AuthTokens?
}
