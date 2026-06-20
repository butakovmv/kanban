package com.kanban.postgres.access

import org.springframework.data.relational.core.mapping.Table

@Table("group_permissions")
internal data class GroupPermissionTable(
    val groupId: String,
    val permissionId: String,
    val grantedAt: java.time.LocalDateTime,
)
