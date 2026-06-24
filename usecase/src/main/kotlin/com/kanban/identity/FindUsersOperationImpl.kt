package com.kanban.identity

internal class FindUsersOperationImpl(
    private val userRepository: UserRepository,
) : FindUsersOperation {
    override suspend fun execute(arg: FindUsersOperation.Arg): FindUsersOperation.Result {
        val users = userRepository.findByIds(arg.userIds)
        return FindUsersOperation.Result(
            users = users.map { UserDisplayInfo(id = it.id.value, displayName = it.displayName) },
        )
    }
}
