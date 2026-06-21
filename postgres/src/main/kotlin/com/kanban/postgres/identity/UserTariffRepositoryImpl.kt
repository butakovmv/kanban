package com.kanban.postgres.identity

import com.kanban.identity.UserTariff
import com.kanban.identity.UserTariffRepository
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository

/**
 * Реализация [UserTariffRepository] через R2DBC и DatabaseClient.
 * Управляет назначениями тарифов пользователям: поиск активного тарифа и сохранение с поддержкой Upsert.
 */
@Repository
internal class UserTariffRepositoryImpl(
    private val db: DatabaseClient,
) : UserTariffRepository {
    /**
     * Поиск активного тарифа пользователя.
     * Возвращает последнюю по дате создания запись, у которой срок действия не истёк или не указан.
     * @param userId идентификатор пользователя
     * @return [UserTariff] или null, если активный тариф не найден
     */
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
            ).bind("userId", UUID.fromString(userId))
            .bind("now", LocalDateTime.now())
            .map { row, _ -> row.toUserTariff() }
            .one()
            .awaitFirstOrNull()

    /**
     * Сохранение назначения тарифа пользователю.
     * Выполняет поиск по идентификатору и в зависимости от результата
     * обновляет существующую запись или вставляет новую.
     * @param userTariff доменная сущность назначения тарифа
     * @return сохранённая сущность [UserTariff]
     */
    override suspend fun save(userTariff: UserTariff): UserTariff {
        val existing = findActiveByUserId(userTariff.userId)
        if (existing != null) {
            updateUserTariff(userTariff)
        } else {
            insertUserTariff(userTariff)
        }
        return userTariff
    }

    /**
     * Обновление существующей записи назначения тарифа.
     */
    private suspend fun updateUserTariff(userTariff: UserTariff) {
        val z = ZoneId.systemDefault()
        db
            .sql(
                """
                UPDATE user_tariffs SET
                    tariff_id = :tariffId, expires_at = :expiresAt
                WHERE id = :id
            """,
            ).bind("id", UUID.fromString(userTariff.id))
            .bind("tariffId", UUID.fromString(userTariff.tariffId))
            .let { spec ->
                val expiresAtLdt = userTariff.expiresAt?.atZone(z)?.toLocalDateTime()
                if (expiresAtLdt != null) {
                    spec.bind("expiresAt", expiresAtLdt)
                } else {
                    spec.bindNull("expiresAt", LocalDateTime::class.java)
                }
            }.fetch()
            .rowsUpdated()
            .awaitSingle()
    }

    /**
     * Вставка новой записи назначения тарифа.
     */
    private suspend fun insertUserTariff(userTariff: UserTariff) {
        val z = ZoneId.systemDefault()
        db
            .sql(
                """
                INSERT INTO user_tariffs (id, user_id, tariff_id, starts_at, expires_at, created_at)
                VALUES (:id, :userId, :tariffId, :startsAt, :expiresAt, :createdAt)
            """,
            ).bind("id", UUID.fromString(userTariff.id))
            .bind("userId", UUID.fromString(userTariff.userId))
            .bind("tariffId", UUID.fromString(userTariff.tariffId))
            .bind("startsAt", userTariff.startsAt.atZone(z).toLocalDateTime())
            .let { spec ->
                val expiresAtLdt = userTariff.expiresAt?.atZone(z)?.toLocalDateTime()
                if (expiresAtLdt != null) {
                    spec.bind("expiresAt", expiresAtLdt)
                } else {
                    spec.bindNull("expiresAt", LocalDateTime::class.java)
                }
            }.bind("createdAt", userTariff.createdAt.atZone(z).toLocalDateTime())
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }

    /**
     * Преобразование строки результата запроса R2DBC в доменную сущность [UserTariff].
     * Считывает колонки таблицы `user_tariffs` и создаёт [UserTariffTable], затем маппит в домен.
     * @param row строка результата запроса
     * @return доменная сущность [UserTariff]
     */
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
