package com.kanban.task

import com.kanban.sse.SinkService
import com.kanban.sse.SseEvent
import java.time.Instant

internal class CommentHandler(
    private val createCommentOperation: CreateCommentOperation,
    private val updateCommentOperation: UpdateCommentOperation,
    private val deleteCommentOperation: DeleteCommentOperation,
    private val listCommentsOperation: ListCommentsOperation,
    private val getTaskOperation: GetTaskOperation,
    private val sinkService: SinkService? = null,
) {
    data class CommentData(
        val id: String,
        val taskId: String,
        val authorId: String,
        val text: String,
        val createdAt: Instant,
        val updatedAt: Instant,
    )

    suspend fun create(
        taskId: String,
        authorId: String,
        text: String,
    ): CreateCommentResult {
        val result =
            createCommentOperation.execute(
                CreateCommentOperation.Arg(
                    taskId = taskId,
                    authorId = authorId,
                    text = text,
                ),
            )
        return when (result) {
            is CreateCommentOperation.Result.Success -> {
                val projectId = resolveProjectId(result.comment.taskId.value)
                val eventData =
                    buildString {
                        append("""{"comment_id":"${result.comment.id.value}",""")
                        append(""""task_id":"${result.comment.taskId.value}"}""")
                    }
                sinkService?.emit(
                    SseEvent(
                        type = "comment_added",
                        data = eventData,
                        boardId = null,
                        projectId = projectId,
                        timestamp = Instant.now(),
                    ),
                )
                CreateCommentResult.Success(
                    comment = result.comment.toData(),
                )
            }
            is CreateCommentOperation.Result.Failure ->
                CreateCommentResult.Failure(reason = result.reason)
        }
    }

    suspend fun update(
        commentId: String,
        text: String,
    ): UpdateCommentResult {
        val result =
            updateCommentOperation.execute(
                UpdateCommentOperation.Arg(
                    commentId = commentId,
                    text = text,
                ),
            )
        return when (result) {
            is UpdateCommentOperation.Result.Success -> {
                val projectId = resolveProjectId(result.comment.taskId.value)
                val eventData =
                    buildString {
                        append("""{"comment_id":"${result.comment.id.value}",""")
                        append(""""task_id":"${result.comment.taskId.value}"}""")
                    }
                sinkService?.emit(
                    SseEvent(
                        type = "comment_updated",
                        data = eventData,
                        boardId = null,
                        projectId = projectId,
                        timestamp = Instant.now(),
                    ),
                )
                UpdateCommentResult.Success(
                    comment = result.comment.toData(),
                )
            }
            UpdateCommentOperation.Result.NotFound -> UpdateCommentResult.NotFound
            is UpdateCommentOperation.Result.Failure ->
                UpdateCommentResult.Failure(reason = result.reason)
        }
    }

    suspend fun delete(commentId: String): DeleteCommentResult {
        val result =
            deleteCommentOperation.execute(
                DeleteCommentOperation.Arg(commentId = commentId),
            )
        return when (result) {
            DeleteCommentOperation.Result.Success -> {
                sinkService?.emit(
                    SseEvent(
                        type = "comment_deleted",
                        data = """{"comment_id":"$commentId"}""",
                        boardId = null,
                        projectId = null,
                        timestamp = Instant.now(),
                    ),
                )
                DeleteCommentResult.Success
            }
            DeleteCommentOperation.Result.NotFound -> DeleteCommentResult.NotFound
        }
    }

    suspend fun list(taskId: String): ListCommentsResult {
        val result =
            listCommentsOperation.execute(
                ListCommentsOperation.Arg(taskId = taskId),
            )
        return when (result) {
            is ListCommentsOperation.Result.Success ->
                ListCommentsResult.Success(
                    comments = result.comments.map { it.toData() },
                )
        }
    }

    private suspend fun resolveProjectId(taskId: String): String? {
        val taskResult =
            getTaskOperation.execute(GetTaskOperation.Arg(taskId = taskId))
        return when (taskResult) {
            is GetTaskOperation.Result.Success -> taskResult.task.projectId.value
            GetTaskOperation.Result.NotFound -> null
        }
    }

    private fun Comment.toData(): CommentData =
        CommentData(
            id = id.value,
            taskId = taskId.value,
            authorId = authorId,
            text = text,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )

    sealed interface CreateCommentResult {
        data class Success(
            val comment: CommentData,
        ) : CreateCommentResult

        data class Failure(
            val reason: String,
        ) : CreateCommentResult
    }

    sealed interface UpdateCommentResult {
        data class Success(
            val comment: CommentData,
        ) : UpdateCommentResult

        data object NotFound : UpdateCommentResult

        data class Failure(
            val reason: String,
        ) : UpdateCommentResult
    }

    sealed interface DeleteCommentResult {
        data object Success : DeleteCommentResult

        data object NotFound : DeleteCommentResult
    }

    sealed interface ListCommentsResult {
        data class Success(
            val comments: List<CommentData>,
        ) : ListCommentsResult
    }
}
