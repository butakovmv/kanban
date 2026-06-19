package com.kanban.identity

/**
 * Сервис отправки email-сообщений.
 * Используется для отправки писем с токенами восстановления пароля и других уведомлений.
 */
interface EmailService {
    /**
     * Отправляет email с токеном восстановления пароля.
     *
     * @param to email-адрес получателя
     * @param recoveryToken токен для сброса пароля
     */
    suspend fun sendRecoveryToken(
        to: String,
        recoveryToken: String,
    )
}
