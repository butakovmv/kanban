package com.kanban.identity

import com.kanban.common.Email
import java.time.Instant

/**
 * Реализация операции обновления пользователя.
 * Находит пользователя по ID, обновляет поля (email с валидацией, displayName) и сохраняет.
 */
internal class UpdateUserOperationImpl(
    private val userRepository: UserRepository,
) : UpdateUserOperation {
    override suspend fun execute(arg: UpdateUserOperation.Arg): UpdateUserOperation.Result {
        val existing =
            userRepository.findById(arg.userId)
                ?: return UpdateUserOperation.Result.NotFound

        val email =
            if (arg.email != null && arg.email != existing.email.value) {
                runCatching { Email(arg.email) }
                    .getOrElse { return UpdateUserOperation.Result.Failure("Invalid email: ${arg.email}") }
            } else {
                existing.email
            }

        val updated =
            existing.copy(
                email = email,
                displayName = arg.displayName ?: existing.displayName,
                updatedAt = Instant.now(),
            )
        val saved = userRepository.save(updated)
        return UpdateUserOperation.Result.Success(saved)
    }
}
