package com.kanban.task

/**
 * Реализация операции удаления комментария.
 * Находит комментарий по ID, при отсутствии возвращает NotFound, иначе удаляет.
 */
internal class DeleteCommentOperationImpl(
    private val commentRepository: CommentRepository,
) : DeleteCommentOperation {
    override suspend fun execute(arg: DeleteCommentOperation.Arg): DeleteCommentOperation.Result {
        val existing =
            commentRepository.findById(arg.commentId) ?: return DeleteCommentOperation.Result.NotFound
        commentRepository.delete(existing.id.value)
        return DeleteCommentOperation.Result.Success
    }
}
