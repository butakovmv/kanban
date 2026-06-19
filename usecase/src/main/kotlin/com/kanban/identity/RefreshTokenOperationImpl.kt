package com.kanban.identity

/**
 * Реализация операции обновления access-токена.
 * Делегирует проверку и обновление токена провайдеру [TokenProvider].
 */
internal class RefreshTokenOperationImpl(
    private val tokenProvider: TokenProvider,
) : RefreshTokenOperation {
    override suspend fun execute(arg: RefreshTokenOperation.Arg): RefreshTokenOperation.Result {
        val tokens =
            tokenProvider.refreshAccessToken(arg.refreshToken)
                ?: return RefreshTokenOperation.Result.Failure("Invalid refresh token")
        return RefreshTokenOperation.Result.Success(tokens)
    }
}
