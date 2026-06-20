package com.kanban.access

/**
 * Реализация операции удаления группы.
 * Каскадно удаляет все членства группы, все связи группы с разрешениями и саму группу.
 */
internal class DeleteGroupOperationImpl(
    private val groupRepository: GroupRepository,
    private val groupMemberRepository: GroupMemberRepository,
    private val groupPermissionRepository: GroupPermissionRepository,
) : DeleteGroupOperation {
    override suspend fun execute(arg: DeleteGroupOperation.Arg): DeleteGroupOperation.Result {
        val existing = groupRepository.findById(arg.groupId) ?: return DeleteGroupOperation.Result.NotFound
        groupMemberRepository.deleteAllByGroup(existing.id.value)
        groupPermissionRepository.deleteAllByGroup(existing.id.value)
        groupRepository.delete(existing.id.value)
        return DeleteGroupOperation.Result.Success
    }
}
