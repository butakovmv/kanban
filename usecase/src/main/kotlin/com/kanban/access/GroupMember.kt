package com.kanban.access

import com.kanban.common.GroupId
import java.time.Instant

/**
 * Связь между группой и пользователем, состоящим в этой группе.
 *
 * @property groupId идентификатор группы
 * @property userId идентификатор пользователя
 * @property addedAt дата и время добавления пользователя в группу
 */
data class GroupMember(
    val groupId: GroupId,
    val userId: String,
    val addedAt: Instant,
)
