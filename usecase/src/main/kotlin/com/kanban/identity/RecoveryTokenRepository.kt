package com.kanban.identity

import java.time.Instant

/**
 * Репозиторий для хранения и управления токенами восстановления пароля.
 * Обеспечивает сохранение, поиск по хешу и удаление токенов.
 */
interface RecoveryTokenRepository {
    /**
     * Сохраняет токен восстановления для пользователя.
     *
     * @param userId идентификатор пользователя
     * @param tokenHash хеш токена
     * @param expiresAt дата истечения токена
     */
    suspend fun save(
        userId: String,
        tokenHash: String,
        expiresAt: Instant,
    )

    /**
     * Находит токен восстановления по хешу.
     *
     * @param tokenHash хеш токена
     * @return пара (идентификатор пользователя, дата истечения) или null
     */
    suspend fun findByTokenHash(tokenHash: String): Pair<String, Instant>?

    /**
     * Удаляет токен восстановления по хешу.
     *
     * @param tokenHash хеш токена для удаления
     */
    suspend fun deleteByTokenHash(tokenHash: String)
}
