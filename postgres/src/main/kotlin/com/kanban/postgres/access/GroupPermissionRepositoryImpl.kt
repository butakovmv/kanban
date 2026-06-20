package com.kanban.postgres.access

import com.kanban.access.GroupPermissionRepository
import com.kanban.access.Permission
import java.time.LocalDateTime
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository

@Repository
internal class GroupPermissionRepositoryImpl(
    private val db: DatabaseClient,
) : GroupPermissionRepository {
    override suspend fun grant(
        groupId: String,
        permissionId: String,
    ) {
        db
            .sql(
                """
                INSERT INTO group_permissions (group_id, permission_id, granted_at)
                VALUES (:groupId, :permissionId, :grantedAt)
                """,
            ).bind("groupId", groupId)
            .bind("permissionId", permissionId)
            .bind("grantedAt", LocalDateTime.now())
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }

    override suspend fun revoke(
        groupId: String,
        permissionId: String,
    ) {
        db
            .sql(
                """
                DELETE FROM group_permissions
                WHERE group_id = :groupId AND permission_id = :permissionId
                """,
            ).bind("groupId", groupId)
            .bind("permissionId", permissionId)
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }

    override suspend fun listPermissionsForGroup(groupId: String): List<Permission> =
        db
            .sql(
                """
                SELECT p.* FROM permissions p
                JOIN group_permissions gp ON p.id = gp.permission_id
                WHERE gp.group_id = :groupId
                ORDER BY p.resource, p.action
                """,
            ).bind("groupId", groupId)
            .map { row, _ -> row.toPermission() }
            .all()
            .collectList()
            .awaitSingle()

    override suspend fun listPermissionsForUser(userId: String): List<Permission> =
        db
            .sql(
                """
                SELECT DISTINCT p.* FROM permissions p
                JOIN group_permissions gp ON p.id = gp.permission_id
                JOIN group_members gm ON gp.group_id = gm.group_id
                WHERE gm.user_id = :userId
                ORDER BY p.resource, p.action
                """,
            ).bind("userId", userId)
            .map { row, _ -> row.toPermission() }
            .all()
            .collectList()
            .awaitSingle()

    override suspend fun deleteAllByGroup(groupId: String) {
        db
            .sql("DELETE FROM group_permissions WHERE group_id = :groupId")
            .bind("groupId", groupId)
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }
}
