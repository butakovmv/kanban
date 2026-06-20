package com.kanban.access

import com.kanban.common.PermissionId
import java.time.Instant
import java.util.UUID

/**
 * Реализация операции создания разрешения.
 * Валидирует входные данные (resource, action) и сохраняет разрешение в репозитории.
 */
internal class CreatePermissionOperationImpl(
    private val permissionRepository: PermissionRepository,
) : CreatePermissionOperation {
    override suspend fun execute(arg: CreatePermissionOperation.Arg): CreatePermissionOperation.Result {
        val validation = validate(arg)
        if (validation != null) return validation

        val permission =
            Permission(
                id = PermissionId(UUID.randomUUID().toString()),
                resource = arg.resource.trim(),
                action = arg.action.trim(),
                targetId = arg.targetId,
                createdAt = Instant.now(),
            )
        val saved = permissionRepository.save(permission)
        return CreatePermissionOperation.Result.Success(saved)
    }

    private fun validate(arg: CreatePermissionOperation.Arg): CreatePermissionOperation.Result.Failure? =
        when {
            arg.resource.isBlank() ->
                CreatePermissionOperation.Result.Failure("Resource must not be blank")
            arg.action.isBlank() ->
                CreatePermissionOperation.Result.Failure("Action must not be blank")
            else -> null
        }
}
