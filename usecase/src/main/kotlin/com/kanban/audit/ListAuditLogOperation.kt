package com.kanban.audit

import com.kanban.common.Operation

interface ListAuditLogOperation : Operation<ListAuditLogOperation.Arg, ListAuditLogOperation.Result> {
    data class Arg(
        val projectId: String,
        val page: Int,
        val size: Int,
    )

    sealed interface Result {
        data class Success(
            val items: List<AuditLog>,
            val total: Long,
        ) : Result
    }
}
