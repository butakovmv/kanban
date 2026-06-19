package com.kanban.identity

import com.kanban.common.AuthTokens

/**
 * Провайдер для генерации и обновления JWT-токенов.
 * Отвечает за создание access и refresh токенов для аутентифицированного пользователя.
 */
interface TokenProvider {
    /**
     * Генерирует пару токенов (access и refresh) для пользователя.
     *
     * @param userId идентификатор пользователя
     * @return сгенерированные токены
     */
    suspend fun generateTokens(userId: String): AuthTokens

    /**
     * Обновляет access-токен с использованием refresh-токена.
     *
     * @param refreshToken refresh-токен
     * @return новая пара токенов или null, если токен недействителен
     */
    suspend fun refreshAccessToken(refreshToken: String): AuthTokens?
}
