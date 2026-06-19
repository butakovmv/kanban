package com.kanban.identity

/**
 * Реализация операции входа по паролю.
 * Находит пользователя по email, проверяет пароль через PasswordHasher и генерирует токены.
 */
internal class LoginWithPasswordOperationImpl(
    private val userRepository: UserRepository,
    private val passwordHasher: PasswordHasher,
    private val tokenProvider: TokenProvider,
) : LoginWithPasswordOperation {
    override suspend fun execute(arg: LoginWithPasswordOperation.Arg): LoginWithPasswordOperation.Result {
        val user =
            userRepository.findByEmail(arg.email)
                ?: return LoginWithPasswordOperation.Result.Failure("Invalid email or password")

        if (!passwordHasher.verify(arg.password, user.passwordHash.value)) {
            return LoginWithPasswordOperation.Result.Failure("Invalid email or password")
        }

        val tokens = tokenProvider.generateTokens(user.id.value)
        return LoginWithPasswordOperation.Result.Success(tokens = tokens, user = user)
    }
}
