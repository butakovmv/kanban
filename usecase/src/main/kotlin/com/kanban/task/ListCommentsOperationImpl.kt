package com.kanban.task

/**
 * Реализация операции получения списка комментариев задачи.
 * Делегирует запрос в репозиторий комментариев.
 */
internal class ListCommentsOperationImpl(
    private val commentRepository: CommentRepository,
) : ListCommentsOperation {
    override suspend fun execute(arg: ListCommentsOperation.Arg): ListCommentsOperation.Result {
        val comments = commentRepository.listByTaskId(arg.taskId)
        return ListCommentsOperation.Result.Success(comments)
    }
}
