package com.kanban.postgres.identity

import java.time.LocalDateTime
import java.util.UUID
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.r2dbc.core.DatabaseClient

internal class UserGenerator(
    private val db: DatabaseClient,
) {
    suspend fun createAndInsert(email: String = "user-${UUID.randomUUID().toString().take(8)}@kanban.test"): String {
        val id = UUID.randomUUID().toString()
        val now = LocalDateTime.now()
        db
            .sql(
                """
            INSERT INTO users (id, email, password_hash, display_name, totp_secret, totp_enabled, created_at, updated_at)
            VALUES (:id, :email, :passwordHash, :displayName, :totpSecret, :totpEnabled, :createdAt, :updatedAt)
        """,
            ).bind("id", id)
            .bind("email", email)
            .bind("passwordHash", "hashed-password")
            .bind("displayName", "Test User ${id.take(8)}")
            .bindNull("totpSecret", String::class.java)
            .bind("totpEnabled", false)
            .bind("createdAt", now)
            .bind("updatedAt", now)
            .fetch()
            .rowsUpdated()
            .awaitSingle()
        return id
    }

    suspend fun deleteAll() {
        db
            .sql("DELETE FROM users")
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }
}
