package com.kanban.postgres.audit

import com.kanban.audit.AuditLog
import com.kanban.audit.AuditLogPage
import com.kanban.audit.AuditLogRepository
import java.time.LocalDateTime
import java.util.UUID
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository

@Repository
internal class AuditLogRepositoryImpl(
    private val db: DatabaseClient,
) : AuditLogRepository {

    override suspend fun save(log: AuditLog): AuditLog {
        val table = AuditLogTable.fromDomain(log)
        db.sql("""
            INSERT INTO audit_log (id, project_id, document_id, user_id, action, details, created_at)
            VALUES (:id, :projectId, :documentId, :userId, :action, :details, :createdAt)
        """)
            .bind("id", UUID.fromString(table.id))
            .let { spec ->
                val pid = table.projectId
                if (pid != null) spec.bind("projectId", UUID.fromString(pid))
                else spec.bindNull("projectId", UUID::class.java)
            }
            .let { spec ->
                val did = table.documentId
                if (did != null) spec.bind("documentId", UUID.fromString(did))
                else spec.bindNull("documentId", UUID::class.java)
            }
            .bind("userId", UUID.fromString(table.userId))
            .bind("action", table.action)
            .let { spec ->
                val d = table.details
                if (d != null) spec.bind("details", d)
                else spec.bindNull("details", String::class.java)
            }
            .bind("createdAt", table.createdAt)
            .fetch()
            .rowsUpdated()
            .awaitSingle()
        return log
    }

    override suspend fun listByProjectId(
        projectId: String,
        page: Int,
        size: Int,
    ): AuditLogPage {
        val offset = (page - 1).coerceAtLeast(0) * size

        val total = db.sql("SELECT COUNT(*) FROM audit_log WHERE project_id = :projectId")
            .bind("projectId", UUID.fromString(projectId))
            .map { row, _ -> row.get("count", java.lang.Long::class.java)!!.toLong() }
            .one()
            .awaitSingle()

        val items = db.sql("""
            SELECT * FROM audit_log
            WHERE project_id = :projectId
            ORDER BY created_at DESC
            LIMIT :limit OFFSET :offset
        """)
            .bind("projectId", UUID.fromString(projectId))
            .bind("limit", size)
            .bind("offset", offset)
            .map { row, _ -> row.toAuditLog() }
            .all()
            .collectList()
            .awaitSingle()

        return AuditLogPage(items = items, total = total)
    }

    private fun io.r2dbc.spi.Row.toAuditLog(): AuditLog {
        val table = AuditLogTable(
            id = get("id", String::class.java)!!,
            projectId = get("project_id", String::class.java),
            documentId = get("document_id", String::class.java),
            userId = get("user_id", String::class.java)!!,
            action = get("action", String::class.java)!!,
            details = get("details", String::class.java),
            createdAt = get("created_at", LocalDateTime::class.java)!!,
        )
        return table.toDomain()
    }
}
