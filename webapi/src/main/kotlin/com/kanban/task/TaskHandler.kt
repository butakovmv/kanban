package com.kanban.task

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

/**
 * Обработчик запросов задач.
 * Связывает HTTP-контроллеры с usecase-операциями: преобразует DTO в аргументы операций,
 * вызывает операции и преобразует результаты обратно в DTO.
 *
 * @property createTaskOperation операция создания задачи
 * @property getTaskOperation операция получения задачи
 * @property listTasksOperation операция получения списка задач
 * @property updateTaskOperation операция обновления задачи
 * @property moveTaskOperation операция перемещения задачи
 * @property archiveTaskOperation операция архивирования задачи
 * @property deleteTaskOperation операция удаления задачи
 */
@Suppress("LongParameterList")
internal class TaskHandler(
    private val createTaskOperation: CreateTaskOperation,
    private val getTaskOperation: GetTaskOperation,
    private val listTasksOperation: ListTasksOperation,
    private val updateTaskOperation: UpdateTaskOperation,
    private val moveTaskOperation: MoveTaskOperation,
    private val archiveTaskOperation: ArchiveTaskOperation,
    private val deleteTaskOperation: DeleteTaskOperation,
) {
    /**
     * Создаёт новую задачу в колонке доски.
     *
     * @param request данные для создания задачи
     * @return результат с созданной задачей или ошибка
     */
    suspend fun create(request: CreateTaskRequest): CreateTaskResult {
        val result =
            createTaskOperation.execute(
                CreateTaskOperation.Arg(
                    boardId = request.boardId,
                    columnId = request.columnId,
                    title = request.title,
                    description = request.description,
                    assigneeId = request.assigneeId,
                    dueDate = request.dueDate,
                ),
            )
        return when (result) {
            is CreateTaskOperation.Result.Success ->
                CreateTaskResult.Success(
                    task = result.task.toResponse(),
                )
            is CreateTaskOperation.Result.Failure ->
                CreateTaskResult.Failure(reason = result.reason)
        }
    }

    /**
     * Получает задачу по идентификатору.
     *
     * @param request идентификатор задачи
     * @return результат с задачей или признак отсутствия
     */
    suspend fun get(request: GetTaskRequest): GetTaskResult {
        val result =
            getTaskOperation.execute(
                GetTaskOperation.Arg(taskId = request.taskId),
            )
        return when (result) {
            is GetTaskOperation.Result.Success ->
                GetTaskResult.Success(
                    task = result.task.toResponse(),
                )
            GetTaskOperation.Result.NotFound -> GetTaskResult.NotFound
        }
    }

    /**
     * Получает список задач доски.
     *
     * @param request параметры запроса списка
     * @return результат со списком задач
     */
    suspend fun list(request: ListTasksRequest): ListTasksResult {
        val result =
            listTasksOperation.execute(
                ListTasksOperation.Arg(
                    boardId = request.boardId,
                    includeArchived = request.includeArchived,
                ),
            )
        return when (result) {
            is ListTasksOperation.Result.Success ->
                ListTasksResult.Success(
                    tasks = result.tasks.map { it.toResponse() },
                )
        }
    }

    /**
     * Обновляет поля задачи.
     *
     * @param request данные для обновления
     * @return результат с обновлённой задачей, ошибка валидации или признак отсутствия
     */
    suspend fun update(request: UpdateTaskRequest): UpdateTaskResult {
        val result =
            updateTaskOperation.execute(
                UpdateTaskOperation.Arg(
                    taskId = request.taskId,
                    title = request.title,
                    description = request.description,
                    assigneeId = request.assigneeId,
                    dueDate = request.dueDate,
                ),
            )
        return when (result) {
            is UpdateTaskOperation.Result.Success ->
                UpdateTaskResult.Success(
                    task = result.task.toResponse(),
                )
            UpdateTaskOperation.Result.NotFound -> UpdateTaskResult.NotFound
            is UpdateTaskOperation.Result.Failure ->
                UpdateTaskResult.Failure(reason = result.reason)
        }
    }

    /**
     * Перемещает задачу в другую колонку и/или на новую позицию.
     *
     * @param request данные для перемещения
     * @return результат с перемещённой задачей, ошибка валидации или признак отсутствия
     */
    suspend fun move(request: MoveTaskRequest): MoveTaskResult {
        val result =
            moveTaskOperation.execute(
                MoveTaskOperation.Arg(
                    taskId = request.taskId,
                    columnId = request.columnId,
                    position = request.position,
                ),
            )
        return when (result) {
            is MoveTaskOperation.Result.Success ->
                MoveTaskResult.Success(
                    task = result.task.toResponse(),
                )
            MoveTaskOperation.Result.NotFound -> MoveTaskResult.NotFound
        }
    }

    /**
     * Архивирует задачу.
     *
     * @param request идентификатор задачи
     * @return результат архивирования
     */
    suspend fun archive(request: ArchiveTaskRequest): ArchiveTaskResult {
        val result =
            archiveTaskOperation.execute(
                ArchiveTaskOperation.Arg(taskId = request.taskId),
            )
        return when (result) {
            ArchiveTaskOperation.Result.Success -> ArchiveTaskResult.Success
            ArchiveTaskOperation.Result.NotFound -> ArchiveTaskResult.NotFound
        }
    }

    /**
     * Удаляет задачу по идентификатору.
     *
     * @param request идентификатор задачи
     * @return результат удаления
     */
    suspend fun delete(request: DeleteTaskRequest): DeleteTaskResult {
        val result =
            deleteTaskOperation.execute(
                DeleteTaskOperation.Arg(taskId = request.taskId),
            )
        return when (result) {
            DeleteTaskOperation.Result.Success -> DeleteTaskResult.Success
            DeleteTaskOperation.Result.NotFound -> DeleteTaskResult.NotFound
        }
    }

    /**
     * Преобразование сущности задачи в DTO ответа.
     */
    private fun Task.toResponse(): TaskResponse =
        TaskResponse(
            id = id.value,
            boardId = boardId.value,
            columnId = columnId.value,
            title = title,
            description = description,
            assigneeId = assigneeId,
            position = position,
            dueDate = dueDate,
            archived = archived,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )

    /**
     * DTO запроса создания задачи.
     *
     * @property boardId идентификатор доски
     * @property columnId идентификатор колонки
     * @property title заголовок задачи
     * @property description описание задачи (опционально)
     * @property assigneeId идентификатор исполнителя (опционально)
     * @property dueDate срок выполнения (опционально)
     */
    data class CreateTaskRequest(
        @JsonProperty("board_id")
        val boardId: String,
        @JsonProperty("column_id")
        val columnId: String,
        val title: String,
        val description: String?,
        @JsonProperty("assignee_id")
        val assigneeId: String?,
        @JsonProperty("due_date")
        val dueDate: Instant?,
    )

    /**
     * DTO запроса получения задачи.
     *
     * @property taskId идентификатор задачи
     */
    data class GetTaskRequest(
        @JsonProperty("task_id")
        val taskId: String,
    )

    /**
     * DTO запроса списка задач доски.
     *
     * @property boardId идентификатор доски
     * @property includeArchived включать ли архивные задачи
     */
    data class ListTasksRequest(
        @JsonProperty("board_id")
        val boardId: String,
        @JsonProperty("include_archived")
        val includeArchived: Boolean,
    )

    /**
     * DTO тела запроса обновления задачи.
     *
     * @property title новый заголовок (null — не изменять)
     * @property description новое описание (null — не изменять)
     * @property assigneeId новый идентификатор исполнителя (null — не изменять)
     * @property dueDate новый срок выполнения (null — не изменять)
     */
    data class UpdateTaskBody(
        val title: String?,
        val description: String?,
        @JsonProperty("assignee_id")
        val assigneeId: String?,
        @JsonProperty("due_date")
        val dueDate: Instant?,
    )

    /**
     * DTO запроса обновления задачи (идентификатор берётся из пути).
     *
     * @property taskId идентификатор задачи
     * @property title новый заголовок (null — не изменять)
     * @property description новое описание (null — не изменять)
     * @property assigneeId новый идентификатор исполнителя (null — не изменять)
     * @property dueDate новый срок выполнения (null — не изменять)
     */
    data class UpdateTaskRequest(
        @JsonProperty("task_id")
        val taskId: String,
        val title: String?,
        val description: String?,
        @JsonProperty("assignee_id")
        val assigneeId: String?,
        @JsonProperty("due_date")
        val dueDate: Instant?,
    )

    /**
     * DTO тела запроса перемещения задачи.
     *
     * @property columnId идентификатор целевой колонки
     * @property position желаемая позиция задачи в целевой колонке
     */
    data class MoveTaskBody(
        @JsonProperty("column_id")
        val columnId: String,
        val position: Int,
    )

    /**
     * DTO запроса перемещения задачи (идентификатор берётся из пути).
     *
     * @property taskId идентификатор задачи
     * @property columnId идентификатор целевой колонки
     * @property position желаемая позиция задачи в целевой колонке
     */
    data class MoveTaskRequest(
        @JsonProperty("task_id")
        val taskId: String,
        @JsonProperty("column_id")
        val columnId: String,
        val position: Int,
    )

    /**
     * DTO запроса архивирования задачи.
     *
     * @property taskId идентификатор задачи
     */
    data class ArchiveTaskRequest(
        @JsonProperty("task_id")
        val taskId: String,
    )

    /**
     * DTO запроса удаления задачи.
     *
     * @property taskId идентификатор задачи
     */
    data class DeleteTaskRequest(
        @JsonProperty("task_id")
        val taskId: String,
    )

    /**
     * DTO ответа с задачей.
     *
     * @property id идентификатор задачи
     * @property boardId идентификатор доски
     * @property columnId идентификатор колонки
     * @property title заголовок задачи
     * @property description описание задачи
     * @property assigneeId идентификатор исполнителя
     * @property position позиция задачи в колонке
     * @property dueDate срок выполнения
     * @property archived признак архивации
     * @property createdAt дата создания
     * @property updatedAt дата последнего изменения
     */
    data class TaskResponse(
        val id: String,
        @JsonProperty("board_id")
        val boardId: String,
        @JsonProperty("column_id")
        val columnId: String,
        val title: String,
        val description: String?,
        @JsonProperty("assignee_id")
        val assigneeId: String?,
        val position: Int,
        @JsonProperty("due_date")
        val dueDate: Instant?,
        val archived: Boolean,
        @JsonProperty("created_at")
        val createdAt: Instant,
        @JsonProperty("updated_at")
        val updatedAt: Instant,
    )

    /**
     * Результат операции создания задачи.
     */
    sealed interface CreateTaskResult {
        /** Задача успешно создана. */
        data class Success(
            val task: TaskResponse,
        ) : CreateTaskResult

        /** Ошибка создания задачи. */
        data class Failure(
            val reason: String,
        ) : CreateTaskResult
    }

    /**
     * Результат операции получения задачи.
     */
    sealed interface GetTaskResult {
        /** Задача найдена. */
        data class Success(
            val task: TaskResponse,
        ) : GetTaskResult

        /** Задача не найдена. */
        data object NotFound : GetTaskResult
    }

    /**
     * Результат операции получения списка задач.
     */
    sealed interface ListTasksResult {
        /** Список задач успешно получен. */
        data class Success(
            val tasks: List<TaskResponse>,
        ) : ListTasksResult
    }

    /**
     * Результат операции обновления задачи.
     */
    sealed interface UpdateTaskResult {
        /** Задача успешно обновлена. */
        data class Success(
            val task: TaskResponse,
        ) : UpdateTaskResult

        /** Задача не найдена. */
        data object NotFound : UpdateTaskResult

        /** Ошибка обновления. */
        data class Failure(
            val reason: String,
        ) : UpdateTaskResult
    }

    /**
     * Результат операции перемещения задачи.
     */
    sealed interface MoveTaskResult {
        /** Задача успешно перемещена. */
        data class Success(
            val task: TaskResponse,
        ) : MoveTaskResult

        /** Задача не найдена. */
        data object NotFound : MoveTaskResult
    }

    /**
     * Результат операции архивирования задачи.
     */
    sealed interface ArchiveTaskResult {
        /** Задача успешно архивирована. */
        data object Success : ArchiveTaskResult

        /** Задача не найдена. */
        data object NotFound : ArchiveTaskResult
    }

    /**
     * Результат операции удаления задачи.
     */
    sealed interface DeleteTaskResult {
        /** Задача успешно удалена. */
        data object Success : DeleteTaskResult

        /** Задача не найдена. */
        data object NotFound : DeleteTaskResult
    }
}
