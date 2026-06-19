package com.kanban.identity

/**
 * Репозиторий для хранения и управления refresh-токенами.
 * Обеспечивает сохранение, поиск по хешу и удаление токенов пользователя.
 */
interface RefreshTokenRepository {
    /**
     * Сохраняет refresh-токен для пользователя.
     *
     * @param userId идентификатор пользователя
     * @param tokenHash хеш токена
     * @param expiresAt дата истечения токена
     */
    suspend fun save(
        userId: String,
        tokenHash: String,
        expiresAt: java.time.Instant,
    )

    /**
     * Находит токен по хешу.
     *
     * @param tokenHash хеш токена
     * @return пара (идентификатор пользователя, дата истечения) или null
     */
    suspend fun findByTokenHash(tokenHash: String): Pair<String, java.time.Instant>?

    /**
     * Удаляет все refresh-токены пользователя.
     *
     * @param userId идентификатор пользователя
     */
    suspend fun deleteByUserId(userId: String)
}
