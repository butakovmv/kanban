package com.kanban.access

import com.kanban.common.GroupId
import com.kanban.common.PermissionId
import java.time.Instant

/**
 * Связь между группой и назначенным ей разрешением.
 *
 * @property groupId идентификатор группы
 * @property permissionId идентификатор разрешения
 * @property grantedAt дата и время выдачи разрешения группе
 */
data class GroupPermission(
    val groupId: GroupId,
    val permissionId: PermissionId,
    val grantedAt: Instant,
)
