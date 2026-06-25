package com.kanban.postgres.task

import com.kanban.task.TaskLabelRepository
import java.util.UUID
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository

/**
 * Реализация [TaskLabelRepository] через R2DBC и DatabaseClient.
 */
@Repository
internal class TaskLabelRepositoryImpl(
    private val db: DatabaseClient,
) : TaskLabelRepository {
    override suspend fun findByTaskId(taskId: String): List<String> =
        db
            .sql("SELECT label FROM task_labels WHERE task_id = :taskId ORDER BY label")
            .bind("taskId", UUID.fromString(taskId))
            .map { row, _ -> row.get("label", String::class.java)!! }
            .all()
            .collectList()
            .awaitSingle()

    override suspend fun findByTaskIds(taskIds: List<String>): Map<String, List<String>> {
        if (taskIds.isEmpty()) return emptyMap()
        val uuids = taskIds.map { UUID.fromString(it) }
        return db
            .sql("SELECT task_id, label FROM task_labels WHERE task_id IN (:taskIds) ORDER BY label")
            .bind("taskIds", uuids)
            .map { row, _ ->
                val tid = row.get("task_id", String::class.java)!!
                val label = row.get("label", String::class.java)!!
                tid to label
            }
            .all()
            .collectList()
            .awaitSingle()
            .groupBy({ it.first }, { it.second })
    }

    override suspend fun save(taskId: String, label: String) {
        db
            .sql("INSERT INTO task_labels (task_id, label) VALUES (:taskId, :label) ON CONFLICT DO NOTHING")
            .bind("taskId", UUID.fromString(taskId))
            .bind("label", label)
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }

    override suspend fun delete(taskId: String, label: String) {
        db
            .sql("DELETE FROM task_labels WHERE task_id = :taskId AND label = :label")
            .bind("taskId", UUID.fromString(taskId))
            .bind("label", label)
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }
}
