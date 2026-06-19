package com.kanban.identity

import com.kanban.common.Email
import com.kanban.common.PasswordHash
import com.kanban.common.UserId
import java.time.Instant
import java.util.UUID

internal class RegisterUserOperationImpl(
    private val userRepository: UserRepository,
    private val passwordHasher: PasswordHasher,
    private val tokenProvider: TokenProvider,
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
        val tokens = tokenProvider.generateTokens(saved.id.value)
        return RegisterUserOperation.Result.Success(tokens = tokens, user = saved)
    }
}
