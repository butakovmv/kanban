package com.kanban.postgres.access

import java.time.LocalDateTime
import java.util.UUID
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.r2dbc.core.DatabaseClient

internal class AccessGenerator(
    private val db: DatabaseClient,
) {
    suspend fun insertGroup(
        name: String = "Group-${UUID.randomUUID().toString().take(8)}",
        description: String? = null,
    ): String {
        val id = UUID.randomUUID().toString()
        val spec =
            db
                .sql(
                    """
                    INSERT INTO groups (id, name, description, created_at)
                    VALUES (:id, :name, :description, :createdAt)
                    """,
                ).bind("id", id)
                .bind("name", name)
                .bind("createdAt", LocalDateTime.now())
        if (description != null) {
            spec
                .bind("description", description)
                .fetch()
                .rowsUpdated()
                .awaitSingle()
        } else {
            spec
                .bindNull("description", String::class.java)
                .fetch()
                .rowsUpdated()
                .awaitSingle()
        }
        return id
    }

    suspend fun insertPermission(
        resource: String = "project",
        action: String = "read",
        targetId: String? = null,
    ): String {
        val id = UUID.randomUUID().toString()
        val spec =
            db
                .sql(
                    """
                    INSERT INTO permissions (id, resource, action, target_id, created_at)
                    VALUES (:id, :resource, :action, :targetId, :createdAt)
                    """,
                ).bind("id", id)
                .bind("resource", resource)
                .bind("action", action)
                .bind("createdAt", LocalDateTime.now())
        if (targetId != null) {
            spec
                .bind("targetId", targetId)
                .fetch()
                .rowsUpdated()
                .awaitSingle()
        } else {
            spec
                .bindNull("targetId", String::class.java)
                .fetch()
                .rowsUpdated()
                .awaitSingle()
        }
        return id
    }

    suspend fun addMember(
        groupId: String,
        userId: String,
    ) {
        db
            .sql(
                """
                INSERT INTO group_members (group_id, user_id, added_at)
                VALUES (:groupId, :userId, :addedAt)
                """,
            ).bind("groupId", groupId)
            .bind("userId", userId)
            .bind("addedAt", LocalDateTime.now())
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }

    suspend fun grantPermission(
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

    suspend fun deleteAll() {
        db
            .sql("DELETE FROM group_permissions")
            .fetch()
            .rowsUpdated()
            .awaitSingle()
        db
            .sql("DELETE FROM group_members")
            .fetch()
            .rowsUpdated()
            .awaitSingle()
        db
            .sql("DELETE FROM permissions")
            .fetch()
            .rowsUpdated()
            .awaitSingle()
        db
            .sql("DELETE FROM groups")
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }
}
