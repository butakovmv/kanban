package com.kanban.identity

import java.security.MessageDigest

/**
 * Реализация операции выхода из системы.
 * Хеширует refresh-токен, находит пользователя по хешу и удаляет все его токены.
 * Операция идемпотентна: повторный выход с уже аннулированным токеном завершается успешно.
 */
internal class LogoutOperationImpl(
    private val refreshTokenRepository: RefreshTokenRepository,
) : LogoutOperation {
    override suspend fun execute(arg: LogoutOperation.Arg): LogoutOperation.Result {
        val tokenHash = sha256(arg.refreshToken)
        val tokenData = refreshTokenRepository.findByTokenHash(tokenHash)

        if (tokenData != null) {
            refreshTokenRepository.deleteByUserId(tokenData.first)
        }

        return LogoutOperation.Result.Success
    }

    /**
     * Вычисляет SHA-256 хеш строки.
     *
     * @param input входная строка
     * @return шестнадцатеричное представление хеша
     */
    private fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
