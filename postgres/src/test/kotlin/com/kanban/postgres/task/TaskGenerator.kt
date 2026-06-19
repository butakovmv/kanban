package com.kanban.postgres.task

import java.time.LocalDateTime
import java.util.UUID
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.r2dbc.core.DatabaseClient

/**
 * Параметры создания тестовой задачи в БД.
 * Используется для удобной передачи опций в [TaskGenerator.createAndInsert]
 * без превышения лимита на количество параметров функции.
 */
internal data class TaskSpec(
    val boardId: String,
    val columnId: String,
    val title: String = "Task-${UUID.randomUUID().toString().take(8)}",
    val description: String? = null,
    val assigneeId: String? = null,
    val position: Int = 0,
    val dueDate: LocalDateTime? = null,
    val archived: Boolean = false,
)

internal class TaskGenerator(
    private val db: DatabaseClient,
) {
    suspend fun createAndInsert(spec: TaskSpec): String {
        val id = UUID.randomUUID().toString()
        val now = LocalDateTime.now()
        val base =
            db
                .sql(
                    """
                    INSERT INTO tasks (id, board_id, column_id, title, description,
                        assignee_id, position, due_date, archived, created_at, updated_at)
                    VALUES (:id, :boardId, :columnId, :title, :description,
                        :assigneeId, :position, :dueDate, :archived, :createdAt, :updatedAt)
                    """,
                ).bind("id", id)
                .bind("boardId", spec.boardId)
                .bind("columnId", spec.columnId)
                .bind("title", spec.title)
                .bind("position", spec.position)
                .bind("archived", spec.archived)
                .bind("createdAt", now)
                .bind("updatedAt", now)
        val withDescription =
            if (spec.description != null) {
                base.bind("description", spec.description)
            } else {
                base.bindNull("description", String::class.java)
            }
        val withAssignee =
            if (spec.assigneeId != null) {
                withDescription.bind("assigneeId", spec.assigneeId)
            } else {
                withDescription.bindNull("assigneeId", String::class.java)
            }
        val withDue =
            if (spec.dueDate != null) {
                withAssignee.bind("dueDate", spec.dueDate)
            } else {
                withAssignee.bindNull("dueDate", LocalDateTime::class.java)
            }
        withDue
            .fetch()
            .rowsUpdated()
            .awaitSingle()
        return id
    }

    suspend fun deleteAll() {
        db
            .sql("DELETE FROM tasks")
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }
}
