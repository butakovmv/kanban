package com.kanban.identity

import com.kanban.common.PasswordHash
import java.security.MessageDigest
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

/**
 * Реализация операции восстановления пароля.
 * Генерирует токен восстановления, отправляет его на email пользователя,
 * а затем позволяет сбросить пароль по предъявленному токену.
 *
 * @property userRepository репозиторий пользователей
 * @property recoveryTokenRepository репозиторий токенов восстановления
 * @property passwordHasher сервис хеширования паролей
 * @property emailService сервис отправки email
 */
internal class RecoveryOperationImpl(
    private val userRepository: UserRepository,
    private val recoveryTokenRepository: RecoveryTokenRepository,
    private val passwordHasher: PasswordHasher,
    private val emailService: EmailService,
) : RecoveryOperation {
    override suspend fun execute(arg: RecoveryOperation.Arg): RecoveryOperation.Result =
        when (arg.action) {
            RecoveryOperation.Action.REQUEST -> requestRecovery(arg.email)
            RecoveryOperation.Action.RESET -> resetPassword(arg.token, arg.newPassword)
        }

    /**
     * Запрашивает токен восстановления для указанного email.
     * Если пользователь существует, генерирует токен, сохраняет его и отправляет на email.
     * Если пользователь не найден, возвращает Success для предотвращения утечки информации.
     */
    private suspend fun requestRecovery(email: String): RecoveryOperation.Result {
        val user =
            userRepository.findByEmail(email)
                ?: return RecoveryOperation.Result.Success(
                    "If the email exists, a recovery token has been sent",
                )

        val rawToken = UUID.randomUUID().toString()
        val tokenHash = sha256(rawToken)
        val expiresAt = Instant.now().plus(1, ChronoUnit.HOURS)

        recoveryTokenRepository.save(
            userId = user.id.value,
            tokenHash = tokenHash,
            expiresAt = expiresAt,
        )

        emailService.sendRecoveryToken(to = user.email.value, recoveryToken = rawToken)

        return RecoveryOperation.Result.Success("Recovery token sent")
    }

    /**
     * Сбрасывает пароль по предъявленному токену.
     */
    private suspend fun resetPassword(
        token: String,
        newPassword: String,
    ): RecoveryOperation.Result {
        val validationError = validateResetInput(token, newPassword)
        val tokenData = if (validationError == null) findValidTokenData(token) else null
        val user = tokenData?.let { userRepository.findById(it.first) }

        return when {
            validationError != null -> validationError
            tokenData == null -> RecoveryOperation.Result.Failure("Invalid or expired token")
            user == null -> RecoveryOperation.Result.Failure("User not found")
            else -> {
                applyPasswordReset(user, newPassword)
                recoveryTokenRepository.deleteByTokenHash(sha256(token))
                RecoveryOperation.Result.Success("Password reset successfully")
            }
        }
    }

    /**
     * Находит данные токена по хешу.
     */
    private suspend fun findValidTokenData(token: String): Pair<String, Instant>? =
        recoveryTokenRepository
            .findByTokenHash(sha256(token))

    /**
     * Валидирует входные данные для сброса пароля.
     * @return null если валидно, или Failure-результат если есть ошибка
     */
    private fun validateResetInput(
        token: String,
        newPassword: String,
    ): RecoveryOperation.Result.Failure? {
        if (token.isBlank()) {
            return RecoveryOperation.Result.Failure("Token is required")
        }
        if (newPassword.isBlank()) {
            return RecoveryOperation.Result.Failure("New password is required")
        }
        return null
    }

    /**
     * Применяет новый пароль к пользователю.
     */
    private suspend fun applyPasswordReset(
        user: User,
        newPassword: String,
    ) {
        val updatedUser =
            user.copy(
                passwordHash = PasswordHash(passwordHasher.hash(newPassword)),
                updatedAt = Instant.now(),
            )
        userRepository.save(updatedUser)
    }

    /**
     * Вычисляет SHA-256 хеш строки.
     */
    private fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
