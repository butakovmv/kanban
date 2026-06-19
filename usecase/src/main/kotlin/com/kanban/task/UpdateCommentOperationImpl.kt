package com.kanban.task

import java.time.Instant

/**
 * Реализация операции обновления комментария.
 * Находит комментарий по ID, обновляет текст и сохраняет.
 */
internal class UpdateCommentOperationImpl(
    private val commentRepository: CommentRepository,
) : UpdateCommentOperation {
    override suspend fun execute(arg: UpdateCommentOperation.Arg): UpdateCommentOperation.Result {
        if (arg.text.isBlank()) {
            return UpdateCommentOperation.Result.Failure("Comment text must not be blank")
        }
        val existing =
            commentRepository.findById(arg.commentId) ?: return UpdateCommentOperation.Result.NotFound
        val updated =
            existing.copy(
                text = arg.text.trim(),
                updatedAt = Instant.now(),
            )
        val saved = commentRepository.save(updated)
        return UpdateCommentOperation.Result.Success(saved)
    }
}
