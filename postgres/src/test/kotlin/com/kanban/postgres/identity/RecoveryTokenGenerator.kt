package com.kanban.postgres.identity

import java.time.LocalDateTime
import java.util.UUID
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.r2dbc.core.DatabaseClient

internal class RecoveryTokenGenerator(
    private val db: DatabaseClient,
) {
    suspend fun createAndInsert(
        userId: String,
        tokenHash: String = "hash-${UUID.randomUUID()}",
        expiresAt: LocalDateTime = LocalDateTime.now().plusDays(1),
    ): String {
        val id = UUID.randomUUID().toString()
        db
            .sql(
                """
                INSERT INTO recovery_tokens (id, user_id, token_hash, expires_at, created_at)
                VALUES (:id, :userId, :tokenHash, :expiresAt, :createdAt)
            """,
            ).bind("id", id)
            .bind("userId", userId)
            .bind("tokenHash", tokenHash)
            .bind("expiresAt", expiresAt)
            .bind("createdAt", LocalDateTime.now())
            .fetch()
            .rowsUpdated()
            .awaitSingle()
        return id
    }

    suspend fun deleteAll() {
        db
            .sql("DELETE FROM recovery_tokens")
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }
}
