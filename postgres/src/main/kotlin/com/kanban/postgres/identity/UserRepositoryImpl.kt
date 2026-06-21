package com.kanban.postgres.identity

import com.kanban.identity.User
import com.kanban.identity.UserRepository
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository

/**
 * Реализация [UserRepository] через R2DBC и DatabaseClient.
 * Реализует паттерн Upsert: при сохранении проверяет существование пользователя
 * и выполняет INSERT или UPDATE соответственно.
 */
@Repository
internal class UserRepositoryImpl(
    private val db: DatabaseClient,
) : UserRepository {
    override suspend fun save(user: User): User {
        val z = ZoneId.systemDefault()
        val createdAt = user.createdAt.atZone(z).toLocalDateTime()
        val updatedAt = user.updatedAt.atZone(z).toLocalDateTime()
        if (findById(user.id.value) != null) {
            updateUser(user, updatedAt)
        } else {
            insertUser(user, createdAt, updatedAt)
        }
        return user
    }

    /**
     * Обновление существующей записи пользователя в таблице `users`.
     * @param user доменная сущность пользователя с обновлёнными данными
     * @param updatedAt метка времени обновления в часовом поясе системы
     */
    private suspend fun updateUser(
        user: User,
        updatedAt: LocalDateTime,
    ) {
        db
            .sql(
                """
                UPDATE users SET
                    email = :email, password_hash = :password_hash,
                    display_name = :display_name, totp_secret = :totp_secret,
                    totp_enabled = :totp_enabled, updated_at = :updated_at
                WHERE id = :id
                """,
            ).bind("email", user.email.value)
            .bind("password_hash", user.passwordHash.value)
            .bind("display_name", user.displayName)
            .let { spec ->
                if (user.totpSecret != null) {
                    spec.bind("totp_secret", user.totpSecret)
                } else {
                    spec.bindNull("totp_secret", String::class.java)
                }
            }.bind("totp_enabled", user.totpEnabled)
            .bind("updated_at", updatedAt)
            .bind("id", UUID.fromString(user.id.value))
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }

    /**
     * Вставка новой записи пользователя в таблицу `users`.
     * @param user доменная сущность пользователя для сохранения
     * @param createdAt метка времени создания
     * @param updatedAt метка времени обновления
     */
    private suspend fun insertUser(
        user: User,
        createdAt: LocalDateTime,
        updatedAt: LocalDateTime,
    ) {
        db
            .sql(
                """
                INSERT INTO users (id, email, password_hash, display_name, totp_secret, totp_enabled, created_at, updated_at)
                VALUES (:id, :email, :password_hash, :display_name, :totp_secret, :totp_enabled, :created_at, :updated_at)
                """,
            ).bind("id", UUID.fromString(user.id.value))
            .bind("email", user.email.value)
            .bind("password_hash", user.passwordHash.value)
            .bind("display_name", user.displayName)
            .let { spec ->
                if (user.totpSecret != null) {
                    spec.bind("totp_secret", user.totpSecret)
                } else {
                    spec.bindNull("totp_secret", String::class.java)
                }
            }.bind("totp_enabled", user.totpEnabled)
            .bind("created_at", createdAt)
            .bind("updated_at", updatedAt)
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }

    /**
     * Поиск пользователя по идентификатору.
     * @param userId строковый идентификатор пользователя
     * @return [User] или null, если пользователь не найден
     */
    override suspend fun findById(userId: String): User? =
        db
            .sql("SELECT * FROM users WHERE id = :id")
            .bind("id", UUID.fromString(userId))
            .map { row, _ -> row.toUser() }
            .one()
            .awaitFirstOrNull()

    /**
     * Поиск пользователя по email.
     * @param email строковый email пользователя
     * @return [User] или null, если пользователь не найден
     */
    override suspend fun findByEmail(email: String): User? =
        db
            .sql("SELECT * FROM users WHERE email = :email")
            .bind("email", email)
            .map { row, _ -> row.toUser() }
            .one()
            .awaitFirstOrNull()

    /**
     * Проверка существования пользователя с указанным email.
     * @param email строковый email для проверки
     * @return true, если пользователь с таким email существует
     */
    override suspend fun existsByEmail(email: String): Boolean =
        db
            .sql("SELECT COUNT(*) as cnt FROM users WHERE email = :email")
            .bind("email", email)
            .map { row, _ -> (row.get("cnt", java.lang.Long::class.java) ?: 0L) as Long }
            .one()
            .awaitSingle() > 0L

    /**
     * Преобразование строки результата запроса R2DBC в доменную сущность [User].
     * Считывает колонки таблицы `users` и создаёт [UserTable], затем маппит в домен.
     * @param row строка результата запроса
     * @return доменная сущность [User]
     */
    private fun io.r2dbc.spi.Row.toUser(): User {
        val table =
            UserTable(
                id = get("id", String::class.java)!!,
                email = get("email", String::class.java)!!,
                passwordHash = get("password_hash", String::class.java)!!,
                displayName = get("display_name", String::class.java)!!,
                totpSecret = get("totp_secret", String::class.java),
                totpEnabled = get("totp_enabled", java.lang.Boolean::class.java)!!.booleanValue(),
                createdAt = get("created_at", java.time.LocalDateTime::class.java)!!,
                updatedAt = get("updated_at", java.time.LocalDateTime::class.java)!!,
            )
        return table.toDomain()
    }
}
