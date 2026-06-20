package com.kanban.access

/**
 * Реализация операции отзыва разрешения у группы.
 * Проверяет существование группы и разрешения, после чего отзывает разрешение.
 */
internal class RevokePermissionOperationImpl(
    private val groupRepository: GroupRepository,
    private val permissionRepository: PermissionRepository,
    private val groupPermissionRepository: GroupPermissionRepository,
) : RevokePermissionOperation {
    override suspend fun execute(arg: RevokePermissionOperation.Arg): RevokePermissionOperation.Result {
        if (groupRepository.findById(arg.groupId) == null) {
            return RevokePermissionOperation.Result.Failure("Group not found")
        }
        if (permissionRepository.findById(arg.permissionId) == null) {
            return RevokePermissionOperation.Result.Failure("Permission not found")
        }
        groupPermissionRepository.revoke(arg.groupId, arg.permissionId)
        return RevokePermissionOperation.Result.Success
    }
}
