package com.kanban.postgres.identity

import java.time.LocalDateTime
import java.util.UUID
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.r2dbc.core.DatabaseClient

internal class UserTariffGenerator(
    private val db: DatabaseClient,
) {
    suspend fun createAndInsert(
        userId: String,
        tariffId: String,
        startsAt: LocalDateTime = LocalDateTime.now().minusDays(1),
        expiresAt: LocalDateTime? = null,
    ): String {
        val id = UUID.randomUUID().toString()
        val spec =
            db
                .sql(
                    """
                INSERT INTO user_tariffs (id, user_id, tariff_id, starts_at, expires_at, created_at)
                VALUES (:id, :userId, :tariffId, :startsAt, :expiresAt, :createdAt)
            """,
                ).bind("id", id)
                .bind("userId", userId)
                .bind("tariffId", tariffId)
                .bind("startsAt", startsAt)
                .bind("createdAt", LocalDateTime.now())
        val finalSpec =
            if (expiresAt != null) {
                spec.bind("expiresAt", expiresAt)
            } else {
                spec.bindNull("expiresAt", LocalDateTime::class.java)
            }
        finalSpec
            .fetch()
            .rowsUpdated()
            .awaitSingle()
        return id
    }

    suspend fun deleteAll() {
        db
            .sql("DELETE FROM user_tariffs")
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }
}
