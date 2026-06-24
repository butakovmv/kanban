package com.kanban.identity

internal class UserHandler(
    private val findUsersOperation: FindUsersOperation,
) {
    suspend fun findUsers(userIds: List<String>): List<UserDisplayInfo> {
        if (userIds.isEmpty()) return emptyList()
        val result = findUsersOperation.execute(FindUsersOperation.Arg(userIds = userIds))
        return result.users
    }
}
