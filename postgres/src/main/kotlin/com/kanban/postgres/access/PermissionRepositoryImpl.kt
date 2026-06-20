package com.kanban.postgres.access

import com.kanban.access.Permission
import com.kanban.access.PermissionRepository
import java.time.LocalDateTime
import java.time.ZoneId
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository

@Repository
internal class PermissionRepositoryImpl(
    private val db: DatabaseClient,
) : PermissionRepository {
    override suspend fun save(permission: Permission): Permission {
        val z = ZoneId.systemDefault()
        val createdAt = permission.createdAt.atZone(z).toLocalDateTime()
        if (findById(permission.id.value) != null) {
            updatePermission(permission)
        } else {
            insertPermission(permission, createdAt)
        }
        return permission
    }

    private suspend fun updatePermission(permission: Permission) {
        db
            .sql(
                """
                UPDATE permissions SET
                    resource = :resource, action = :action, target_id = :targetId
                WHERE id = :id
                """,
            ).bind("id", permission.id.value)
            .bind("resource", permission.resource)
            .bind("action", permission.action)
            .let { spec ->
                val targetId = permission.targetId
                if (targetId != null) {
                    spec.bind("targetId", targetId)
                } else {
                    spec.bindNull("targetId", String::class.java)
                }
            }.fetch()
            .rowsUpdated()
            .awaitSingle()
    }

    private suspend fun insertPermission(
        permission: Permission,
        createdAt: LocalDateTime,
    ) {
        db
            .sql(
                """
                INSERT INTO permissions (id, resource, action, target_id, created_at)
                VALUES (:id, :resource, :action, :targetId, :createdAt)
                """,
            ).bind("id", permission.id.value)
            .bind("resource", permission.resource)
            .bind("action", permission.action)
            .let { spec ->
                val targetId = permission.targetId
                if (targetId != null) {
                    spec.bind("targetId", targetId)
                } else {
                    spec.bindNull("targetId", String::class.java)
                }
            }.bind("createdAt", createdAt)
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }

    override suspend fun findById(id: String): Permission? =
        db
            .sql("SELECT * FROM permissions WHERE id = :id")
            .bind("id", id)
            .map { row, _ -> row.toPermission() }
            .one()
            .awaitFirstOrNull()

    override suspend fun findByResource(
        resource: String,
        targetId: String?,
    ): List<Permission> {
        val sql =
            if (targetId != null) {
                "SELECT * FROM permissions WHERE resource = :resource AND target_id = :targetId"
            } else {
                "SELECT * FROM permissions WHERE resource = :resource"
            }
        val spec = db.sql(sql).bind("resource", resource)
        return if (targetId != null) {
            spec
                .bind("targetId", targetId)
                .map { row, _ -> row.toPermission() }
                .all()
                .collectList()
                .awaitSingle()
        } else {
            spec
                .map { row, _ -> row.toPermission() }
                .all()
                .collectList()
                .awaitSingle()
        }
    }

    override suspend fun delete(id: String) {
        db
            .sql("DELETE FROM permissions WHERE id = :id")
            .bind("id", id)
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }
}
