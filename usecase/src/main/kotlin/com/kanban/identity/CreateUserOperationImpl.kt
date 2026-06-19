package com.kanban.identity

import com.kanban.common.Email
import com.kanban.common.PasswordHash
import com.kanban.common.UserId
import java.time.Instant
import java.util.UUID

internal class CreateUserOperationImpl(
    private val userRepository: UserRepository,
) : CreateUserOperation {
    override suspend fun execute(arg: CreateUserOperation.Arg): CreateUserOperation.Result {
        val email =
            runCatching { Email(arg.email) }
                .getOrElse { return CreateUserOperation.Result.Failure("Invalid email: ${arg.email}") }

        if (userRepository.existsByEmail(arg.email)) {
            return CreateUserOperation.Result.Failure("Email already registered")
        }

        val user =
            User(
                id = UserId(UUID.randomUUID().toString()),
                email = email,
                passwordHash = PasswordHash(arg.passwordHash),
                displayName = arg.displayName,
                totpSecret = null,
                totpEnabled = false,
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
            )

        val saved = userRepository.save(user)
        return CreateUserOperation.Result.Success(saved)
    }
}
