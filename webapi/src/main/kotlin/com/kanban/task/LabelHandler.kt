package com.kanban.task

import com.kanban.sse.SinkService
import com.kanban.sse.SseEvent
import java.time.Instant

internal class LabelHandler(
    private val taskLabelRepository: TaskLabelRepository,
    private val getTaskOperation: GetTaskOperation,
    private val sinkService: SinkService? = null,
) {
    suspend fun add(
        taskId: String,
        label: String,
    ): AddLabelResult {
        taskLabelRepository.save(taskId, label)
        val projectId = resolveProjectId(taskId)
        sinkService?.emit(
            SseEvent(
                type = "label_added",
                data = """{"task_id":"$taskId","label":"$label","project_id":"$projectId"}""",
                boardId = null,
                projectId = projectId,
                timestamp = Instant.now(),
            ),
        )
        return AddLabelResult.Success
    }

    suspend fun remove(
        taskId: String,
        label: String,
    ): RemoveLabelResult {
        taskLabelRepository.delete(taskId, label)
        val projectId = resolveProjectId(taskId)
        sinkService?.emit(
            SseEvent(
                type = "label_removed",
                data = """{"task_id":"$taskId","label":"$label","project_id":"$projectId"}""",
                boardId = null,
                projectId = projectId,
                timestamp = Instant.now(),
            ),
        )
        return RemoveLabelResult.Success
    }

    private suspend fun resolveProjectId(taskId: String): String? {
        val taskResult =
            getTaskOperation.execute(GetTaskOperation.Arg(taskId = taskId))
        return when (taskResult) {
            is GetTaskOperation.Result.Success -> taskResult.task.projectId.value
            GetTaskOperation.Result.NotFound -> null
        }
    }

    sealed interface AddLabelResult {
        data object Success : AddLabelResult
    }

    sealed interface RemoveLabelResult {
        data object Success : RemoveLabelResult
    }
}
