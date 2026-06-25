package com.kanban.task

import com.kanban.audit.LogAuditEventOperation
import com.kanban.sse.SinkService
import com.kanban.sse.SseEvent
import java.time.Instant
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

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
    private val logAuditEventOperation: LogAuditEventOperation,
    private val sinkService: SinkService? = null,
    private val taskLabelRepository: TaskLabelRepository,
) {
    data class TaskData(
        val id: String,
        val projectId: String,
        val columnId: String,
        val title: String,
        val description: String?,
        val assigneeId: String?,
        val position: Int,
        val dueDate: Instant?,
        val priority: String?,
        val archived: Boolean,
        val createdAt: Instant,
        val updatedAt: Instant,
        val labels: List<String>,
    )

    suspend fun create(
        projectId: String,
        columnId: String,
        title: String,
        description: String?,
        assigneeId: String?,
        dueDate: Instant?,
        priority: String?,
        userId: String?,
    ): CreateTaskResult {
        val result =
            createTaskOperation.execute(
                CreateTaskOperation.Arg(
                    projectId = projectId,
                    columnId = columnId,
                    title = title,
                    description = description,
                    assigneeId = assigneeId,
                    dueDate = dueDate,
                    priority = priority,
                ),
            )
        return when (result) {
            is CreateTaskOperation.Result.Success -> {
                if (userId != null) {
                    logAuditEventOperation.execute(
                        LogAuditEventOperation.Arg(
                            projectId = projectId,
                            documentId = null,
                            userId = userId,
                            action = "task.created",
                            details = """{"task_id":"${result.task.id.value}","title":"${result.task.title}"}""",
                        ),
                    )
                }
                sinkService?.emit(
                    SseEvent(
                        type = "task_created",
                        data = """{"task_id":"${result.task.id.value}","project_id":"${result.task.projectId.value}"}""",
                        boardId = null,
                        projectId = result.task.projectId.value,
                        timestamp = Instant.now(),
                    ),
                )
                CreateTaskResult.Success(
                    task = result.task.toData(emptyList()),
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
            is GetTaskOperation.Result.Success -> {
                val labels = taskLabelRepository.findByTaskId(taskId)
                GetTaskResult.Success(
                    task = result.task.toData(labels),
                )
            }
            GetTaskOperation.Result.NotFound -> GetTaskResult.NotFound
        }
    }

    suspend fun list(
        projectId: String,
        includeArchived: Boolean,
    ): ListTasksResult {
        val result =
            listTasksOperation.execute(
                ListTasksOperation.Arg(
                    projectId = projectId,
                    includeArchived = includeArchived,
                ),
            )
        return when (result) {
            is ListTasksOperation.Result.Success -> {
                val labelMap = loadLabels(result.tasks.map { it.id.value })
                ListTasksResult.Success(
                    tasks = result.tasks.map { it.toData(labelMap[it.id.value] ?: emptyList()) },
                )
            }
        }
    }

    private suspend fun loadLabels(taskIds: List<String>): Map<String, List<String>> {
        if (taskIds.isEmpty()) return emptyMap()
        return taskLabelRepository.findByTaskIds(taskIds)
    }

    suspend fun listBoardBacklog(projectId: String): ListBoardBacklogResult {
        val result =
            listBoardBacklogOperation.execute(
                ListBoardBacklogOperation.Arg(
                    projectId = projectId,
                ),
            )
        return when (result) {
            is ListBoardBacklogOperation.Result.Success -> {
                val labelMap = loadLabels(result.tasks.map { it.id.value })
                ListBoardBacklogResult.Success(
                    tasks = result.tasks.map { it.toData(labelMap[it.id.value] ?: emptyList()) },
                )
            }
        }
    }

    suspend fun listArchivedTasks(projectId: String): ListArchivedTasksResult {
        val result =
            listArchivedTasksOperation.execute(
                ListArchivedTasksOperation.Arg(
                    projectId = projectId,
                ),
            )
        return when (result) {
            is ListArchivedTasksOperation.Result.Success -> {
                val labelMap = loadLabels(result.tasks.map { it.id.value })
                ListArchivedTasksResult.Success(
                    tasks = result.tasks.map { it.toData(labelMap[it.id.value] ?: emptyList()) },
                )
            }
        }
    }

    suspend fun update(
        taskId: String,
        title: String?,
        description: String?,
        assigneeId: String?,
        dueDate: Instant?,
        priority: String?,
        userId: String?,
        projectId: String?,
    ): UpdateTaskResult {
        val result =
            updateTaskOperation.execute(
                UpdateTaskOperation.Arg(
                    taskId = taskId,
                    title = title,
                    description = description,
                    assigneeId = assigneeId,
                    dueDate = dueDate,
                    priority = priority,
                ),
            )
        return when (result) {
            is UpdateTaskOperation.Result.Success -> {
                if (userId != null && projectId != null) {
                    logAuditEventOperation.execute(
                        LogAuditEventOperation.Arg(
                            projectId = projectId,
                            documentId = null,
                            userId = userId,
                            action = "task.updated",
                            details = """{"task_id":"${result.task.id.value}"}""",
                        ),
                    )
                }
                sinkService?.emit(
                    SseEvent(
                        type = "task_updated",
                        data = """{"task_id":"${result.task.id.value}","project_id":"${result.task.projectId.value}"}""",
                        boardId = null,
                        projectId = result.task.projectId.value,
                        timestamp = Instant.now(),
                    ),
                )
                val labels = taskLabelRepository.findByTaskId(result.task.id.value)
                UpdateTaskResult.Success(
                    task = result.task.toData(labels),
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
        userId: String?,
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
                if (userId != null) {
                    logAuditEventOperation.execute(
                        LogAuditEventOperation.Arg(
                            projectId = result.task.projectId.value,
                            documentId = null,
                            userId = userId,
                            action = "task.moved",
                            details = """{"task_id":"${result.task.id.value}","column_id":"${result.task.columnId.value}"}""",
                        ),
                    )
                }
                val eventData =
                    buildString {
                        append("""{"task_id":"${result.task.id.value}",""")
                        append(""""project_id":"${result.task.projectId.value}",""")
                        append(""""column_id":"${result.task.columnId.value}"}""")
                    }
                sinkService?.emit(
                    SseEvent(
                        type = "task_moved",
                        data = eventData,
                        boardId = null,
                        projectId = result.task.projectId.value,
                        timestamp = Instant.now(),
                    ),
                )
                val labels = taskLabelRepository.findByTaskId(result.task.id.value)
                MoveTaskResult.Success(
                    task = result.task.toData(labels),
                )
            }
            MoveTaskOperation.Result.NotFound -> MoveTaskResult.NotFound
        }
    }

    suspend fun archive(taskId: String, userId: String?, projectId: String?): ArchiveTaskResult {
        val result =
            archiveTaskOperation.execute(
                ArchiveTaskOperation.Arg(taskId = taskId),
            )
        return when (result) {
            ArchiveTaskOperation.Result.Success -> {
                if (userId != null && projectId != null) {
                    logAuditEventOperation.execute(
                        LogAuditEventOperation.Arg(
                            projectId = projectId,
                            documentId = null,
                            userId = userId,
                            action = "task.archived",
                            details = """{"task_id":"${taskId}"}""",
                        ),
                    )
                }
                sinkService?.emit(
                    SseEvent(
                        type = "task_archived",
                        data = """{"task_id":"${taskId}"}""",
                        boardId = null,
                        projectId = projectId,
                        timestamp = Instant.now(),
                    ),
                )
                ArchiveTaskResult.Success
            }
            ArchiveTaskOperation.Result.NotFound -> ArchiveTaskResult.NotFound
        }
    }

    suspend fun delete(taskId: String, userId: String?, projectId: String?): DeleteTaskResult {
        val result =
            deleteTaskOperation.execute(
                DeleteTaskOperation.Arg(taskId = taskId),
            )
        return when (result) {
            DeleteTaskOperation.Result.Success -> {
                if (userId != null && projectId != null) {
                    logAuditEventOperation.execute(
                        LogAuditEventOperation.Arg(
                            projectId = projectId,
                            documentId = null,
                            userId = userId,
                            action = "task.deleted",
                            details = """{"task_id":"${taskId}"}""",
                        ),
                    )
                }
                sinkService?.emit(
                    SseEvent(
                        type = "task_deleted",
                        data = """{"task_id":"${taskId}"}""",
                        boardId = null,
                        projectId = projectId,
                        timestamp = Instant.now(),
                    ),
                )
                DeleteTaskResult.Success
            }
            DeleteTaskOperation.Result.NotFound -> DeleteTaskResult.NotFound
        }
    }

    private fun Task.toData(labels: List<String> = emptyList()): TaskData =
        TaskData(
            id = id.value,
            projectId = projectId.value,
            columnId = columnId.value,
            title = title,
            description = description,
            assigneeId = assigneeId,
            position = position,
            dueDate = dueDate,
            priority = priority,
            archived = archived,
            createdAt = createdAt,
            updatedAt = updatedAt,
            labels = labels,
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
