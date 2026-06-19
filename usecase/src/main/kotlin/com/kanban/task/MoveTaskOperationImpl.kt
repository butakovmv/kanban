package com.kanban.task

import com.kanban.common.ColumnId
import java.time.Instant

/**
 * Реализация операции перемещения задачи.
 * Находит задачу, обновляет её колонку и позицию, пересчитывает позиции остальных задач
 * в исходной и целевой колонках, после чего сохраняет обновлённый набор позиций через репозиторий.
 */
internal class MoveTaskOperationImpl(
    private val taskRepository: TaskRepository,
) : MoveTaskOperation {
    override suspend fun execute(arg: MoveTaskOperation.Arg): MoveTaskOperation.Result {
        val task = taskRepository.findById(arg.taskId) ?: return MoveTaskOperation.Result.NotFound
        val sourceColumnId = task.columnId.value
        val targetColumnId = arg.columnId
        val moved =
            task.copy(
                columnId = ColumnId(targetColumnId),
                position = arg.position,
                updatedAt = Instant.now(),
            )

        val sourceTasks =
            if (sourceColumnId == targetColumnId) {
                emptyList()
            } else {
                taskRepository.listByColumnId(sourceColumnId).filter { it.id.value != task.id.value }
            }

        val targetExcludingMoved =
            taskRepository.listByColumnId(targetColumnId).filter { it.id.value != task.id.value }
        val clampedPosition = arg.position.coerceIn(0, targetExcludingMoved.size)
        val newTarget =
            targetExcludingMoved
                .toMutableList()
                .apply { add(clampedPosition, moved) }
                .mapIndexed { index, item -> item.copy(position = index) }

        val toUpdate =
            if (sourceColumnId == targetColumnId) {
                newTarget
            } else {
                newTarget + sourceTasks.mapIndexed { index, item -> item.copy(position = index) }
            }

        taskRepository.updatePositions(toUpdate)
        return MoveTaskOperation.Result.Success(moved)
    }
}
