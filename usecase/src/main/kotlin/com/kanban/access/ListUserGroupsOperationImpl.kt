package com.kanban.access

/**
 * Реализация операции получения списка групп пользователя.
 * Делегирует запрос в репозиторий членства.
 */
internal class ListUserGroupsOperationImpl(
    private val groupMemberRepository: GroupMemberRepository,
) : ListUserGroupsOperation {
    override suspend fun execute(arg: ListUserGroupsOperation.Arg): ListUserGroupsOperation.Result {
        val groups = groupMemberRepository.listGroupsForUser(arg.userId)
        return ListUserGroupsOperation.Result.Success(groups)
    }
}
