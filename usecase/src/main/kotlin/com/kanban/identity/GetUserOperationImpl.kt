package com.kanban.identity

/**
 * Реализация операции получения пользователя.
 * Делегирует поиск в репозиторий пользователей.
 */
internal class GetUserOperationImpl(
    private val userRepository: UserRepository,
) : GetUserOperation {
    override suspend fun execute(arg: GetUserOperation.Arg): GetUserOperation.Result {
        val user = userRepository.findById(arg.userId) ?: return GetUserOperation.Result.NotFound
        return GetUserOperation.Result.Success(user)
    }
}
