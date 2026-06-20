package com.kanban.access

/**
 * Реализация операции удаления пользователя из группы.
 * Проверяет существование группы, наличие пользователя в группе и удаляет членство.
 */
internal class RemoveMemberOperationImpl(
    private val groupRepository: GroupRepository,
    private val groupMemberRepository: GroupMemberRepository,
) : RemoveMemberOperation {
    override suspend fun execute(arg: RemoveMemberOperation.Arg): RemoveMemberOperation.Result {
        if (groupRepository.findById(arg.groupId) == null) {
            return RemoveMemberOperation.Result.Failure("Group not found")
        }
        if (!groupMemberRepository.isMember(arg.groupId, arg.userId)) {
            return RemoveMemberOperation.Result.Failure("User is not a member of the group")
        }
        groupMemberRepository.removeMember(arg.groupId, arg.userId)
        return RemoveMemberOperation.Result.Success
    }
}
