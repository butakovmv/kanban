package com.kanban.access

/**
 * Реализация операции получения списка членов группы.
 * Делегирует запрос в репозиторий членства.
 */
internal class ListMembersOperationImpl(
    private val groupMemberRepository: GroupMemberRepository,
) : ListMembersOperation {
    override suspend fun execute(arg: ListMembersOperation.Arg): ListMembersOperation.Result {
        val members = groupMemberRepository.listMembers(arg.groupId)
        return ListMembersOperation.Result.Success(members)
    }
}
