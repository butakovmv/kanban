package com.kanban.task

import com.kanban.sse.SinkService
import com.kanban.sse.SseEvent
import java.time.Instant

@Suppress("LongParameterList")
internal class TaskHandler(
    private val createTaskOperation: CreateTaskOperation,
    private val getTaskOperation: GetTaskOperation,
    private val listTasksOperation: ListTasksOperation,
    private val listBoardBacklogOperation: ListBoardBacklogOperation,
    private val listArchivedTasksOperation: ListArchivedTasksOperation,
    private val updateTaskOperation: UpdateTaskOperation,
    private val moveTaskOperation: MoveTaskOperation,
    private val archiveTaskOperation: ArchiveTaskOperation,
    private val deleteTaskOperation: DeleteTaskOperation,
    private val sinkService: SinkService? = null,
) {
    data class TaskData(
        val id: String,
        val boardId: String,
        val columnId: String,
        val title: String,
        val description: String?,
        val assigneeId: String?,
        val position: Int,
        val dueDate: Instant?,
        val archived: Boolean,
        val createdAt: Instant,
        val updatedAt: Instant,
    )

    suspend fun create(
        boardId: String,
        columnId: String,
        title: String,
        description: String?,
        assigneeId: String?,
        dueDate: Instant?,
    ): CreateTaskResult {
        val result =
            createTaskOperation.execute(
                CreateTaskOperation.Arg(
                    boardId = boardId,
                    columnId = columnId,
                    title = title,
                    description = description,
                    assigneeId = assigneeId,
                    dueDate = dueDate,
                ),
            )
        return when (result) {
            is CreateTaskOperation.Result.Success -> {
                sinkService?.emit(
                    SseEvent(
                        type = "task_created",
                        data = """{"task_id":"${result.task.id.value}","board_id":"${result.task.boardId.value}"}""",
                        boardId = result.task.boardId.value,
                        projectId = null,
                        timestamp = Instant.now(),
                    ),
                )
                CreateTaskResult.Success(
                    task = result.task.toData(),
                )
            }
            is CreateTaskOperation.Result.Failure ->
                CreateTaskResult.Failure(reason = result.reason)
        }
    }

    suspend fun get(taskId: String): GetTaskResult {
        val result =
            getTaskOperation.execute(
                GetTaskOperation.Arg(taskId = taskId),
            )
        return when (result) {
            is GetTaskOperation.Result.Success ->
                GetTaskResult.Success(
                    task = result.task.toData(),
                )
            GetTaskOperation.Result.NotFound -> GetTaskResult.NotFound
        }
    }

    suspend fun list(
        boardId: String,
        includeArchived: Boolean,
    ): ListTasksResult {
        val result =
            listTasksOperation.execute(
                ListTasksOperation.Arg(
                    boardId = boardId,
                    includeArchived = includeArchived,
                ),
            )
        return when (result) {
            is ListTasksOperation.Result.Success ->
                ListTasksResult.Success(
                    tasks = result.tasks.map { it.toData() },
                )
        }
    }

    suspend fun listBoardBacklog(boardId: String): ListBoardBacklogResult {
        val result =
            listBoardBacklogOperation.execute(
                ListBoardBacklogOperation.Arg(
                    boardId = boardId,
                ),
            )
        return when (result) {
            is ListBoardBacklogOperation.Result.Success ->
                ListBoardBacklogResult.Success(
                    tasks = result.tasks.map { it.toData() },
                )
        }
    }

    suspend fun listArchivedTasks(boardId: String): ListArchivedTasksResult {
        val result =
            listArchivedTasksOperation.execute(
                ListArchivedTasksOperation.Arg(
                    boardId = boardId,
                ),
            )
        return when (result) {
            is ListArchivedTasksOperation.Result.Success ->
                ListArchivedTasksResult.Success(
                    tasks = result.tasks.map { it.toData() },
                )
        }
    }

    suspend fun update(
        taskId: String,
        title: String?,
        description: String?,
        assigneeId: String?,
        dueDate: Instant?,
    ): UpdateTaskResult {
        val result =
            updateTaskOperation.execute(
                UpdateTaskOperation.Arg(
                    taskId = taskId,
                    title = title,
                    description = description,
                    assigneeId = assigneeId,
                    dueDate = dueDate,
                ),
            )
        return when (result) {
            is UpdateTaskOperation.Result.Success -> {
                sinkService?.emit(
                    SseEvent(
                        type = "task_updated",
                        data = """{"task_id":"${result.task.id.value}","board_id":"${result.task.boardId.value}"}""",
                        boardId = result.task.boardId.value,
                        projectId = null,
                        timestamp = Instant.now(),
                    ),
                )
                UpdateTaskResult.Success(
                    task = result.task.toData(),
                )
            }
            UpdateTaskOperation.Result.NotFound -> UpdateTaskResult.NotFound
            is UpdateTaskOperation.Result.Failure ->
                UpdateTaskResult.Failure(reason = result.reason)
        }
    }

    suspend fun move(
        taskId: String,
        columnId: String,
        position: Int,
    ): MoveTaskResult {
        val result =
            moveTaskOperation.execute(
                MoveTaskOperation.Arg(
                    taskId = taskId,
                    columnId = columnId,
                    position = position,
                ),
            )
        return when (result) {
            is MoveTaskOperation.Result.Success -> {
                val eventData =
                    buildString {
                        append("""{"task_id":"${result.task.id.value}",""")
                        append(""""board_id":"${result.task.boardId.value}",""")
                        append(""""column_id":"${result.task.columnId.value}"}""")
                    }
                sinkService?.emit(
                    SseEvent(
                        type = "task_moved",
                        data = eventData,
                        boardId = result.task.boardId.value,
                        projectId = null,
                        timestamp = Instant.now(),
                    ),
                )
                MoveTaskResult.Success(
                    task = result.task.toData(),
                )
            }
            MoveTaskOperation.Result.NotFound -> MoveTaskResult.NotFound
        }
    }

    suspend fun archive(taskId: String): ArchiveTaskResult {
        val result =
            archiveTaskOperation.execute(
                ArchiveTaskOperation.Arg(taskId = taskId),
            )
        return when (result) {
            ArchiveTaskOperation.Result.Success -> {
                sinkService?.emit(
                    SseEvent(
                        type = "task_archived",
                        data = """{"task_id":"${taskId}"}""",
                        boardId = null,
                        projectId = null,
                        timestamp = Instant.now(),
                    ),
                )
                ArchiveTaskResult.Success
            }
            ArchiveTaskOperation.Result.NotFound -> ArchiveTaskResult.NotFound
        }
    }

    suspend fun delete(taskId: String): DeleteTaskResult {
        val result =
            deleteTaskOperation.execute(
                DeleteTaskOperation.Arg(taskId = taskId),
            )
        return when (result) {
            DeleteTaskOperation.Result.Success -> {
                sinkService?.emit(
                    SseEvent(
                        type = "task_deleted",
                        data = """{"task_id":"${taskId}"}""",
                        boardId = null,
                        projectId = null,
                        timestamp = Instant.now(),
                    ),
                )
                DeleteTaskResult.Success
            }
            DeleteTaskOperation.Result.NotFound -> DeleteTaskResult.NotFound
        }
    }

    private fun Task.toData(): TaskData =
        TaskData(
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

    sealed interface CreateTaskResult {
        data class Success(
            val task: TaskData,
        ) : CreateTaskResult

        data class Failure(
            val reason: String,
        ) : CreateTaskResult
    }

    sealed interface GetTaskResult {
        data class Success(
            val task: TaskData,
        ) : GetTaskResult

        data object NotFound : GetTaskResult
    }

    sealed interface ListTasksResult {
        data class Success(
            val tasks: List<TaskData>,
        ) : ListTasksResult
    }

    sealed interface ListBoardBacklogResult {
        data class Success(
            val tasks: List<TaskData>,
        ) : ListBoardBacklogResult
    }

    sealed interface ListArchivedTasksResult {
        data class Success(
            val tasks: List<TaskData>,
        ) : ListArchivedTasksResult
    }

    sealed interface UpdateTaskResult {
        data class Success(
            val task: TaskData,
        ) : UpdateTaskResult

        data object NotFound : UpdateTaskResult

        data class Failure(
            val reason: String,
        ) : UpdateTaskResult
    }

    sealed interface MoveTaskResult {
        data class Success(
            val task: TaskData,
        ) : MoveTaskResult

        data object NotFound : MoveTaskResult
    }

    sealed interface ArchiveTaskResult {
        data object Success : ArchiveTaskResult

        data object NotFound : ArchiveTaskResult
    }

    sealed interface DeleteTaskResult {
        data object Success : DeleteTaskResult

        data object NotFound : DeleteTaskResult
    }
}
