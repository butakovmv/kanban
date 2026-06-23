package com.kanban.document

import com.kanban.common.Operation

interface UpdateDocumentOperation : Operation<UpdateDocumentOperation.Arg, UpdateDocumentOperation.Result> {
    data class Arg(
        val documentId: String,
        val path: String?,
        val title: String?,
        val content: String?,
        val description: String?,
    )

    sealed interface Result {
        data class Success(
            val document: Document,
        ) : Result

        data object NotFound : Result

        data class Failure(
            val reason: String,
        ) : Result
    }
}
