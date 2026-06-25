package com.kanban.postgres.access

import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("group_permissions")
internal data class GroupPermissionTable(
    @Column("group_id")
    val groupId: String,
    @Column("permission_id")
    val permissionId: String,
    @Column("granted_at")
    val grantedAt: java.time.LocalDateTime,
)
