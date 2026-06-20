package com.kanban.access

/**
 * Реализация операции выдачи разрешения группе.
 * Проверяет существование группы и разрешения, после чего выдаёт разрешение.
 */
internal class GrantPermissionOperationImpl(
    private val groupRepository: GroupRepository,
    private val permissionRepository: PermissionRepository,
    private val groupPermissionRepository: GroupPermissionRepository,
) : GrantPermissionOperation {
    override suspend fun execute(arg: GrantPermissionOperation.Arg): GrantPermissionOperation.Result {
        if (groupRepository.findById(arg.groupId) == null) {
            return GrantPermissionOperation.Result.Failure("Group not found")
        }
        if (permissionRepository.findById(arg.permissionId) == null) {
            return GrantPermissionOperation.Result.Failure("Permission not found")
        }
        groupPermissionRepository.grant(arg.groupId, arg.permissionId)
        return GrantPermissionOperation.Result.Success
    }
}
