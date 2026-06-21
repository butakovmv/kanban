package com.kanban.postgres.identity

import com.kanban.identity.RefreshTokenRepository
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository

/**
 * Реализация [RefreshTokenRepository] через R2DBC и DatabaseClient.
 * Управляет refresh-токенами: сохранение новых, поиск по хешу и удаление по идентификатору пользователя.
 */
@Repository
internal class RefreshTokenRepositoryImpl(
    private val db: DatabaseClient,
) : RefreshTokenRepository {
    /**
     * Сохранение нового refresh-токена для пользователя.
     * Генерирует уникальный идентификатор через UUID.
     * @param userId идентификатор пользователя
     * @param tokenHash хеш токена
     * @param expiresAt момент истечения токена
     */
    override suspend fun save(
        userId: String,
        tokenHash: String,
        expiresAt: Instant,
    ) {
        db
            .sql(
                """
            INSERT INTO refresh_tokens (id, user_id, token_hash, expires_at, created_at)
            VALUES (:id, :userId, :tokenHash, :expiresAt, :createdAt)
        """,
            ).bind("id", UUID.randomUUID())
            .bind("userId", UUID.fromString(userId))
            .bind("tokenHash", tokenHash)
            .bind("expiresAt", expiresAt.atZone(ZoneOffset.UTC).toLocalDateTime())
            .bind("createdAt", LocalDateTime.now())
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }

    /**
     * Поиск действующего refresh-токена по хешу.
     * Учитывает только токены, срок действия которых ещё не истёк.
     * @param tokenHash хеш токена для поиска
     * @return пара (идентификатор пользователя, момент истечения) или null, если токен не найден или истёк
     */
    override suspend fun findByTokenHash(tokenHash: String): Pair<String, Instant>? =
        db
            .sql(
                """
            SELECT * FROM refresh_tokens WHERE token_hash = :tokenHash AND expires_at > :now
        """,
            ).bind("tokenHash", tokenHash)
            .bind("now", LocalDateTime.now())
            .map { row, _ ->
                val userId = row.get("user_id", String::class.java)!!
                val expiresAt = row.get("expires_at", LocalDateTime::class.java)!!.toInstant(ZoneOffset.UTC)
                userId to expiresAt
            }.one()
            .awaitFirstOrNull()

    /**
     * Удаление всех refresh-токенов указанного пользователя.
     * Используется при выходе из системы или отзыве всех сессий.
     * @param userId идентификатор пользователя
     */
    override suspend fun deleteByUserId(userId: String) {
        db
            .sql("DELETE FROM refresh_tokens WHERE user_id = :userId")
            .bind("userId", UUID.fromString(userId))
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }
}
