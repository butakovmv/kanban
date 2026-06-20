package com.kanban.access

/**
 * Реализация операции получения списка разрешений группы.
 * Делегирует запрос в репозиторий связей «группа-разрешение».
 */
internal class ListGroupPermissionsOperationImpl(
    private val groupPermissionRepository: GroupPermissionRepository,
) : ListGroupPermissionsOperation {
    override suspend fun execute(arg: ListGroupPermissionsOperation.Arg): ListGroupPermissionsOperation.Result {
        val permissions = groupPermissionRepository.listPermissionsForGroup(arg.groupId)
        return ListGroupPermissionsOperation.Result.Success(permissions)
    }
}
