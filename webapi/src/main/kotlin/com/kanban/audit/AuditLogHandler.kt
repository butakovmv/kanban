package com.kanban.audit

import java.time.Instant

internal class AuditLogHandler(
    private val listAuditLogOperation: ListAuditLogOperation,
    private val logAuditEventOperation: LogAuditEventOperation,
) {
    data class AuditEntry(
        val id: String,
        val projectId: String?,
        val documentId: String?,
        val userId: String,
        val action: String,
        val details: String?,
        val createdAt: Instant,
    )

    suspend fun list(
        projectId: String,
        page: Int,
        size: Int,
    ): ListAuditLogResult {
        val result =
            listAuditLogOperation.execute(
                ListAuditLogOperation.Arg(
                    projectId = projectId,
                    page = page,
                    size = size,
                ),
            )
        return when (result) {
            is ListAuditLogOperation.Result.Success ->
                ListAuditLogResult.Success(
                    items = result.items.map { it.toEntry() },
                    total = result.total,
                )
        }
    }

    suspend fun log(
        projectId: String?,
        documentId: String?,
        userId: String,
        action: String,
        details: String?,
    ) {
        logAuditEventOperation.execute(
            LogAuditEventOperation.Arg(
                projectId = projectId,
                documentId = documentId,
                userId = userId,
                action = action,
                details = details,
            ),
        )
    }

    private fun AuditLog.toEntry(): AuditEntry =
        AuditEntry(
            id = id,
            projectId = projectId,
            documentId = documentId,
            userId = userId,
            action = action,
            details = details,
            createdAt = createdAt,
        )

    sealed interface ListAuditLogResult {
        data class Success(
            val items: List<AuditEntry>,
            val total: Long,
        ) : ListAuditLogResult
    }
}
