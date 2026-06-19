package com.kanban.identity

interface RefreshTokenRepository {
    suspend fun save(
        userId: String,
        tokenHash: String,
        expiresAt: java.time.Instant,
    )

    suspend fun findByTokenHash(tokenHash: String): Pair<String, java.time.Instant>?

    suspend fun deleteByUserId(userId: String)
}
