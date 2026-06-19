package com.kanban.postgres.identity

import com.kanban.identity.UserTariff
import com.kanban.identity.UserTariffRepository
import java.time.LocalDateTime
import java.time.ZoneId
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository

@Repository
internal class UserTariffRepositoryImpl(
    private val db: DatabaseClient,
) : UserTariffRepository {
    override suspend fun findActiveByUserId(userId: String): UserTariff? =
        db
            .sql(
                """
            SELECT * FROM user_tariffs
            WHERE user_id = :userId
              AND (expires_at IS NULL OR expires_at > :now)
            ORDER BY created_at DESC
            LIMIT 1
        """,
            ).bind("userId", userId)
            .bind("now", LocalDateTime.now())
            .map { row, _ -> row.toUserTariff() }
            .one()
            .awaitFirstOrNull()

    override suspend fun save(userTariff: UserTariff): UserTariff {
        db
            .sql(
                """
            INSERT INTO user_tariffs (id, user_id, tariff_id, starts_at, expires_at, created_at)
            VALUES (:id, :userId, :tariffId, :startsAt, :expiresAt, :createdAt)
            ON CONFLICT (id) DO UPDATE SET
                tariff_id = EXCLUDED.tariff_id,
                expires_at = EXCLUDED.expires_at
        """,
            ).bind("id", userTariff.id)
            .bind("userId", userTariff.userId)
            .bind("tariffId", userTariff.tariffId)
            .bind("startsAt", userTariff.startsAt.atZone(ZoneId.systemDefault()).toLocalDateTime())
            .bind("expiresAt", userTariff.expiresAt?.atZone(ZoneId.systemDefault())?.toLocalDateTime())
            .bind("createdAt", userTariff.createdAt.atZone(ZoneId.systemDefault()).toLocalDateTime())
            .fetch()
            .rowsUpdated()
            .awaitFirstOrNull()
        return userTariff
    }

    private fun io.r2dbc.spi.Row.toUserTariff(): UserTariff {
        val table =
            UserTariffTable(
                id = get("id", String::class.java)!!,
                userId = get("user_id", String::class.java)!!,
                tariffId = get("tariff_id", String::class.java)!!,
                startsAt = get("starts_at", java.time.LocalDateTime::class.java)!!,
                expiresAt = get("expires_at", java.time.LocalDateTime::class.java),
                createdAt = get("created_at", java.time.LocalDateTime::class.java)!!,
            )
        return table.toDomain()
    }
}
