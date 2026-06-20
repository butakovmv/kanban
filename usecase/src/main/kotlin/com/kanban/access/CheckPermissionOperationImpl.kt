package com.kanban.access

/**
 * Реализация операции проверки разрешения.
 * Получает все разрешения пользователя через группы и ищет подходящее:
 * совпадение по [resource] и [action], а также совпадение по [targetId] —
 * либо targetId равен null у разрешения (глобальное), либо совпадает с запрошенным.
 */
internal class CheckPermissionOperationImpl(
    private val groupPermissionRepository: GroupPermissionRepository,
) : CheckPermissionOperation {
    override suspend fun execute(arg: CheckPermissionOperation.Arg): CheckPermissionOperation.Result {
        val permissions = groupPermissionRepository.listPermissionsForUser(arg.userId)
        val matched =
            permissions.any { permission ->
                permission.resource == arg.resource &&
                    permission.action == arg.action &&
                    (permission.targetId == null || permission.targetId == arg.targetId)
            }
        return if (matched) {
            CheckPermissionOperation.Result.Allowed
        } else {
            CheckPermissionOperation.Result.Denied(
                "User ${arg.userId} has no permission '${arg.action}' on ${arg.resource}" +
                    if (arg.targetId != null) " (target=${arg.targetId})" else "",
            )
        }
    }
}
