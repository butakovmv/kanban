package com.kanban.audit

import com.kanban.common.Operation

interface LogAuditEventOperation : Operation<LogAuditEventOperation.Arg, LogAuditEventOperation.Result> {
    data class Arg(
        val projectId: String?,
        val documentId: String?,
        val userId: String,
        val action: String,
        val details: String?,
    )

    sealed interface Result {
        data object Success : Result
    }
}
