package com.kanban.access

/**
 * Реализация операции поиска разрешений.
 * Делегирует запрос в репозиторий разрешений.
 */
internal class FindPermissionsOperationImpl(
    private val permissionRepository: PermissionRepository,
) : FindPermissionsOperation {
    override suspend fun execute(arg: FindPermissionsOperation.Arg): FindPermissionsOperation.Result {
        val permissions = permissionRepository.findByResource(arg.resource, arg.targetId)
        return FindPermissionsOperation.Result.Success(permissions)
    }
}
