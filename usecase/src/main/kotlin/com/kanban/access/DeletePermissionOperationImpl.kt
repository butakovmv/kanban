package com.kanban.access

/**
 * Реализация операции удаления разрешения.
 * Находит разрешение по ID, при отсутствии возвращает NotFound, иначе удаляет.
 */
internal class DeletePermissionOperationImpl(
    private val permissionRepository: PermissionRepository,
) : DeletePermissionOperation {
    override suspend fun execute(arg: DeletePermissionOperation.Arg): DeletePermissionOperation.Result {
        val existing =
            permissionRepository.findById(arg.permissionId)
                ?: return DeletePermissionOperation.Result.NotFound
        permissionRepository.delete(existing.id.value)
        return DeletePermissionOperation.Result.Success
    }
}
