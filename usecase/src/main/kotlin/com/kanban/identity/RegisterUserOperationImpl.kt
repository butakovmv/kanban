package com.kanban.identity

import com.kanban.common.Email
import com.kanban.common.PasswordHash
import com.kanban.common.UserId
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Реализация операции регистрации пользователя.
 * Валидирует email, проверяет уникальность, хеширует пароль, создаёт пользователя и генерирует токены.
 * После успешного создания пользователю назначается тариф "Free".
 */
internal class RegisterUserOperationImpl(
    private val userRepository: UserRepository,
    private val passwordHasher: PasswordHasher,
    private val tokenProvider: TokenProvider,
    private val userTariffRepository: UserTariffRepository,
    private val tariffRepository: TariffRepository,
) : RegisterUserOperation {
    override suspend fun execute(arg: RegisterUserOperation.Arg): RegisterUserOperation.Result {
        val email =
            runCatching { Email(arg.email) }
                .getOrElse { return RegisterUserOperation.Result.Failure("Invalid email: ${arg.email}") }

        if (userRepository.existsByEmail(arg.email)) {
            return RegisterUserOperation.Result.Failure("Email already registered")
        }

        val passwordHash = passwordHasher.hash(arg.password)

        val user =
            User(
                id = UserId(UUID.randomUUID().toString()),
                email = email,
                passwordHash = PasswordHash(passwordHash),
                displayName = arg.displayName,
                totpSecret = null,
                totpEnabled = false,
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
            )

        val saved = userRepository.save(user)
        assignFreeTariff(saved.id.value)
        val tokens = tokenProvider.generateTokens(saved.id.value)
        return RegisterUserOperation.Result.Success(tokens = tokens, user = saved)
    }

    private val freeTariffMutex = Mutex()

    private suspend fun assignFreeTariff(userId: String) {
        val freeTariff = freeTariffMutex.withLock {
            var tariff = tariffRepository.findByName("Free")
            if (tariff == null) {
                tariff =
                    Tariff(
                        id = com.kanban.common.TariffId(UUID.randomUUID().toString()),
                        name = "Free",
                        limits = TariffLimits(5, 3, 50, 10, 100),
                        createdAt = Instant.now(),
                    )
                tariffRepository.save(tariff)
            }
            tariff
        }

        val userTariff =
            UserTariff(
                id = UUID.randomUUID().toString(),
                userId = userId,
                tariffId = freeTariff.id.value,
                startsAt = Instant.now(),
                expiresAt = null,
                createdAt = Instant.now(),
            )
        userTariffRepository.save(userTariff)
    }
}
