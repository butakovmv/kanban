package com.kanban.postgres.identity

import com.kanban.identity.RecoveryTokenRepository
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository

@Repository
internal class RecoveryTokenRepositoryImpl(
    private val db: DatabaseClient,
) : RecoveryTokenRepository {

    override suspend fun save(
        userId: String,
        tokenHash: String,
        expiresAt: Instant,
    ) {
        db
            .sql(
                """
            INSERT INTO recovery_tokens (id, user_id, token_hash, expires_at, created_at)
            VALUES (:id, :userId, :tokenHash, :expiresAt, :createdAt)
        """,
            ).bind("id", UUID.randomUUID().toString())
            .bind("userId", userId)
            .bind("tokenHash", tokenHash)
            .bind("expiresAt", expiresAt.atZone(ZoneOffset.UTC).toLocalDateTime())
            .bind("createdAt", LocalDateTime.now())
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }

    override suspend fun findByTokenHash(tokenHash: String): Pair<String, Instant>? =
        db
            .sql(
                """
            SELECT * FROM recovery_tokens WHERE token_hash = :tokenHash AND expires_at > :now
        """,
            ).bind("tokenHash", tokenHash)
            .bind("now", Instant.now().atZone(ZoneOffset.UTC).toLocalDateTime())
            .map { row, _ ->
                val userId = row.get("user_id", String::class.java)!!
                val expiresAt = row.get("expires_at", LocalDateTime::class.java)!!.toInstant(ZoneOffset.UTC)
                userId to expiresAt
            }.one()
            .awaitFirstOrNull()

    override suspend fun deleteByTokenHash(tokenHash: String) {
        db
            .sql("DELETE FROM recovery_tokens WHERE token_hash = :tokenHash")
            .bind("tokenHash", tokenHash)
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }
}
