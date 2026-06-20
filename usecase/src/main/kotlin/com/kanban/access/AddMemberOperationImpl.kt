package com.kanban.access

/**
 * Реализация операции добавления пользователя в группу.
 * Проверяет существование группы, отсутствие пользователя в группе и добавляет членство.
 */
internal class AddMemberOperationImpl(
    private val groupRepository: GroupRepository,
    private val groupMemberRepository: GroupMemberRepository,
) : AddMemberOperation {
    override suspend fun execute(arg: AddMemberOperation.Arg): AddMemberOperation.Result {
        if (groupRepository.findById(arg.groupId) == null) {
            return AddMemberOperation.Result.Failure("Group not found")
        }
        if (groupMemberRepository.isMember(arg.groupId, arg.userId)) {
            return AddMemberOperation.Result.Failure("User is already a member of the group")
        }
        groupMemberRepository.addMember(arg.groupId, arg.userId)
        return AddMemberOperation.Result.Success
    }
}
