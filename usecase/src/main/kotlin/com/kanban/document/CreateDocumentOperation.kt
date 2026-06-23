package com.kanban.document

import com.kanban.common.Operation

interface CreateDocumentOperation : Operation<CreateDocumentOperation.Arg, CreateDocumentOperation.Result> {
    data class Arg(
        val projectId: String,
        val path: String,
        val title: String,
        val content: String,
        val description: String?,
    )

    sealed interface Result {
        data class Success(
            val document: Document,
        ) : Result

        data class Failure(
            val reason: String,
        ) : Result
    }
}
