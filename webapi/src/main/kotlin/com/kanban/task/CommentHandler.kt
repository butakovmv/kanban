package com.kanban.task

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

/**
 * Обработчик запросов комментариев.
 * Связывает HTTP-контроллеры с usecase-операциями: преобразует DTO в аргументы операций,
 * вызывает операции и преобразует результаты обратно в DTO.
 *
 * @property createCommentOperation операция создания комментария
 * @property updateCommentOperation операция обновления комментария
 * @property deleteCommentOperation операция удаления комментария
 * @property listCommentsOperation операция получения списка комментариев
 */
internal class CommentHandler(
    private val createCommentOperation: CreateCommentOperation,
    private val updateCommentOperation: UpdateCommentOperation,
    private val deleteCommentOperation: DeleteCommentOperation,
    private val listCommentsOperation: ListCommentsOperation,
) {
    /**
     * Создаёт комментарий к задаче.
     *
     * @param request данные для создания комментария
     * @return результат с созданным комментарием или ошибка
     */
    suspend fun create(request: CreateCommentRequest): CreateCommentResult {
        val result =
            createCommentOperation.execute(
                CreateCommentOperation.Arg(
                    taskId = request.taskId,
                    authorId = request.authorId,
                    text = request.text,
                ),
            )
        return when (result) {
            is CreateCommentOperation.Result.Success ->
                CreateCommentResult.Success(
                    comment = result.comment.toResponse(),
                )
            is CreateCommentOperation.Result.Failure ->
                CreateCommentResult.Failure(reason = result.reason)
        }
    }

    /**
     * Обновляет текст комментария.
     *
     * @param request данные для обновления
     * @return результат с обновлённым комментарием, ошибка валидации или признак отсутствия
     */
    suspend fun update(request: UpdateCommentRequest): UpdateCommentResult {
        val result =
            updateCommentOperation.execute(
                UpdateCommentOperation.Arg(
                    commentId = request.commentId,
                    text = request.text,
                ),
            )
        return when (result) {
            is UpdateCommentOperation.Result.Success ->
                UpdateCommentResult.Success(
                    comment = result.comment.toResponse(),
                )
            UpdateCommentOperation.Result.NotFound -> UpdateCommentResult.NotFound
            is UpdateCommentOperation.Result.Failure ->
                UpdateCommentResult.Failure(reason = result.reason)
        }
    }

    /**
     * Удаляет комментарий по идентификатору.
     *
     * @param request идентификатор комментария
     * @return результат удаления
     */
    suspend fun delete(request: DeleteCommentRequest): DeleteCommentResult {
        val result =
            deleteCommentOperation.execute(
                DeleteCommentOperation.Arg(commentId = request.commentId),
            )
        return when (result) {
            DeleteCommentOperation.Result.Success -> DeleteCommentResult.Success
            DeleteCommentOperation.Result.NotFound -> DeleteCommentResult.NotFound
        }
    }

    /**
     * Получает список комментариев задачи.
     *
     * @param request идентификатор задачи
     * @return результат со списком комментариев
     */
    suspend fun list(request: ListCommentsRequest): ListCommentsResult {
        val result =
            listCommentsOperation.execute(
                ListCommentsOperation.Arg(taskId = request.taskId),
            )
        return when (result) {
            is ListCommentsOperation.Result.Success ->
                ListCommentsResult.Success(
                    comments = result.comments.map { it.toResponse() },
                )
        }
    }

    /**
     * Преобразование сущности комментария в DTO ответа.
     */
    private fun Comment.toResponse(): CommentResponse =
        CommentResponse(
            id = id.value,
            taskId = taskId.value,
            authorId = authorId,
            text = text,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )

    /**
     * DTO запроса создания комментария.
     *
     * @property taskId идентификатор задачи
     * @property authorId идентификатор автора
     * @property text текст комментария
     */
    data class CreateCommentRequest(
        @JsonProperty("task_id")
        val taskId: String,
        @JsonProperty("author_id")
        val authorId: String,
        val text: String,
    )

    /**
     * DTO тела запроса обновления комментария.
     *
     * @property text новый текст комментария
     */
    data class UpdateCommentBody(
        val text: String,
    )

    /**
     * DTO запроса обновления комментария (идентификатор берётся из пути).
     *
     * @property commentId идентификатор комментария
     * @property text новый текст комментария
     */
    data class UpdateCommentRequest(
        @JsonProperty("comment_id")
        val commentId: String,
        val text: String,
    )

    /**
     * DTO запроса удаления комментария.
     *
     * @property commentId идентификатор комментария
     */
    data class DeleteCommentRequest(
        @JsonProperty("comment_id")
        val commentId: String,
    )

    /**
     * DTO запроса списка комментариев.
     *
     * @property taskId идентификатор задачи
     */
    data class ListCommentsRequest(
        @JsonProperty("task_id")
        val taskId: String,
    )

    /**
     * DTO ответа с комментарием.
     *
     * @property id идентификатор комментария
     * @property taskId идентификатор задачи
     * @property authorId идентификатор автора
     * @property text текст комментария
     * @property createdAt дата создания
     * @property updatedAt дата последнего изменения
     */
    data class CommentResponse(
        val id: String,
        @JsonProperty("task_id")
        val taskId: String,
        @JsonProperty("author_id")
        val authorId: String,
        val text: String,
        @JsonProperty("created_at")
        val createdAt: Instant,
        @JsonProperty("updated_at")
        val updatedAt: Instant,
    )

    /**
     * Результат операции создания комментария.
     */
    sealed interface CreateCommentResult {
        /** Комментарий успешно создан. */
        data class Success(
            val comment: CommentResponse,
        ) : CreateCommentResult

        /** Ошибка создания комментария. */
        data class Failure(
            val reason: String,
        ) : CreateCommentResult
    }

    /**
     * Результат операции обновления комментария.
     */
    sealed interface UpdateCommentResult {
        /** Комментарий успешно обновлён. */
        data class Success(
            val comment: CommentResponse,
        ) : UpdateCommentResult

        /** Комментарий не найден. */
        data object NotFound : UpdateCommentResult

        /** Ошибка обновления. */
        data class Failure(
            val reason: String,
        ) : UpdateCommentResult
    }

    /**
     * Результат операции удаления комментария.
     */
    sealed interface DeleteCommentResult {
        /** Комментарий успешно удалён. */
        data object Success : DeleteCommentResult

        /** Комментарий не найден. */
        data object NotFound : DeleteCommentResult
    }

    /**
     * Результат операции получения списка комментариев.
     */
    sealed interface ListCommentsResult {
        /** Список комментариев успешно получен. */
        data class Success(
            val comments: List<CommentResponse>,
        ) : ListCommentsResult
    }
}
