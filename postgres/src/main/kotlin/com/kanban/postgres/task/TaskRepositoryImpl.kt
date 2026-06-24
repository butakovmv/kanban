package com.kanban.postgres.task

import com.kanban.task.Task
import com.kanban.task.TaskRepository
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository

/**
 * Реализация [TaskRepository] через R2DBC и DatabaseClient.
 * Реализует паттерн Upsert: при сохранении проверяет существование задачи
 * и выполняет INSERT или UPDATE соответственно, сохраняя признак архивирования.
 */
@Repository
internal class TaskRepositoryImpl(
    private val db: DatabaseClient,
) : TaskRepository {
    /**
     * Сохранение задачи (создание или обновление).
     * При обновлении сохраняется текущий признак архивирования из БД,
     * если он не установлен явно в доменной сущности.
     * @param task доменная сущность задачи
     * @return сохранённая задача
     */
    override suspend fun save(task: Task): Task {
        val z = ZoneId.systemDefault()
        val createdAt = task.createdAt.atZone(z).toLocalDateTime()
        val existing = findRawArchivedById(task.id.value)
        val archived = existing?.archived ?: task.archived
        if (existing != null) {
            updateTask(task, createdAt, archived)
        } else {
            insertTask(task, createdAt)
        }
        return task.copy(archived = archived)
    }

    /**
     * Обновление существующей записи задачи в таблице `tasks`.
     * @param task доменная сущность задачи с обновлёнными данными
     * @param updatedAt метка времени обновления
     * @param archived текущий признак архивирования
     */
    private suspend fun updateTask(
        task: Task,
        updatedAt: LocalDateTime,
        archived: Boolean,
    ) {
        val z = ZoneId.systemDefault()
        db
            .sql(
                """
                UPDATE tasks SET
                    project_id = :projectId, column_id = :columnId,
                    title = :title, description = :description,
                    assignee_id = :assigneeId, position = :position,
                    due_date = :dueDate, priority = :priority,
                    archived = :archived, updated_at = :updatedAt
                WHERE id = :id
                """,
            ).bind("id", UUID.fromString(task.id.value))
            .bind("projectId", UUID.fromString(task.projectId.value))
            .bind("columnId", UUID.fromString(task.columnId.value))
            .bind("title", task.title)
            .let { spec ->
                val description = task.description
                if (description != null) {
                    spec.bind("description", description)
                } else {
                    spec.bindNull("description", String::class.java)
                }
            }.let { spec ->
                val assignee = task.assigneeId
                if (assignee != null) {
                    spec.bind("assigneeId", UUID.fromString(assignee))
                } else {
                    spec.bindNull("assigneeId", UUID::class.java)
                }
            }.bind("position", task.position)
            .let { spec ->
                val due = task.dueDate?.atZone(z)?.toLocalDateTime()
                if (due != null) {
                    spec.bind("dueDate", due)
                } else {
                    spec.bindNull("dueDate", LocalDateTime::class.java)
                }
            }.let { spec ->
                val priority = task.priority
                if (priority != null) {
                    spec.bind("priority", priority)
                } else {
                    spec.bindNull("priority", String::class.java)
                }
            }.bind("archived", archived)
            .bind("updatedAt", updatedAt)
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }

    /**
     * Вставка новой записи задачи в таблицу `tasks`.
     * @param task доменная сущность задачи для сохранения
     * @param createdAt метка времени создания
     */
    private suspend fun insertTask(
        task: Task,
        createdAt: LocalDateTime,
    ) {
        val z = ZoneId.systemDefault()
        db
            .sql(
                """
                INSERT INTO tasks (id, project_id, column_id, title, description,
                    assignee_id, position, due_date, priority, archived, created_at, updated_at)
                VALUES (:id, :projectId, :columnId, :title, :description,
                    :assigneeId, :position, :dueDate, :priority, :archived, :createdAt, :updatedAt)
                """,
            ).bind("id", UUID.fromString(task.id.value))
            .bind("projectId", UUID.fromString(task.projectId.value))
            .bind("columnId", UUID.fromString(task.columnId.value))
            .bind("title", task.title)
            .let { spec ->
                val description = task.description
                if (description != null) {
                    spec.bind("description", description)
                } else {
                    spec.bindNull("description", String::class.java)
                }
            }.let { spec ->
                val assignee = task.assigneeId
                if (assignee != null) {
                    spec.bind("assigneeId", UUID.fromString(assignee))
                } else {
                    spec.bindNull("assigneeId", UUID::class.java)
                }
            }.bind("position", task.position)
            .let { spec ->
                val due = task.dueDate?.atZone(z)?.toLocalDateTime()
                if (due != null) {
                    spec.bind("dueDate", due)
                } else {
                    spec.bindNull("dueDate", LocalDateTime::class.java)
                }
            }.let { spec ->
                val priority = task.priority
                if (priority != null) {
                    spec.bind("priority", priority)
                } else {
                    spec.bindNull("priority", String::class.java)
                }
            }.bind("archived", task.archived)
            .bind("createdAt", createdAt)
            .bind("updatedAt", createdAt)
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }

    /**
     * Поиск задачи по идентификатору.
     * @param id строковый идентификатор задачи
     * @return [Task] или null, если задача не найдена
     */
    override suspend fun findById(id: String): Task? =
        db
            .sql("SELECT * FROM tasks WHERE id = :id")
            .bind("id", UUID.fromString(id))
            .map { row, _ -> row.toTask() }
            .one()
            .awaitFirstOrNull()

    /**
     * Чтение сырого значения флага `archived` из БД по идентификатору.
     * @param id строковый идентификатор задачи
     * @return true/false или null, если запись не найдена
     */
    private suspend fun findRawArchivedById(id: String): ArchivedRow? =
        db
            .sql("SELECT archived FROM tasks WHERE id = :id")
            .bind("id", UUID.fromString(id))
            .map { row, _ ->
                ArchivedRow(archived = row.get("archived", java.lang.Boolean::class.java)!!.booleanValue())
            }.one()
            .awaitFirstOrNull()

    /**
     * Получение списка задач указанного проекта, упорядоченных по позиции.
     * По умолчанию архивные задачи исключаются из результата.
     * @param projectId идентификатор проекта
     * @param includeArchived включать ли архивные задачи
     * @return список [Task] проекта
     */
    override suspend fun listByProjectId(
        projectId: String,
        includeArchived: Boolean,
    ): List<Task> {
        val sql =
            if (includeArchived) {
                "SELECT * FROM tasks WHERE project_id = :projectId ORDER BY position"
            } else {
                "SELECT * FROM tasks WHERE project_id = :projectId AND archived = FALSE ORDER BY position"
            }
        return db
            .sql(sql)
            .bind("projectId", UUID.fromString(projectId))
            .map { row, _ -> row.toTask() }
            .all()
            .collectList()
            .awaitSingle()
    }

    /**
     * Получение списка задач указанной колонки, упорядоченных по позиции.
     * @param columnId идентификатор колонки
     * @return список [Task] колонки
     */
    override suspend fun listByColumnId(columnId: String): List<Task> =
        db
            .sql("SELECT * FROM tasks WHERE column_id = :columnId AND archived = FALSE ORDER BY position")
            .bind("columnId", UUID.fromString(columnId))
            .map { row, _ -> row.toTask() }
            .all()
            .collectList()
            .awaitSingle()

    /**
     * Удаление задачи по идентификатору.
     * @param id идентификатор задачи
     */
    override suspend fun delete(id: String) {
        db
            .sql("DELETE FROM tasks WHERE id = :id")
            .bind("id", UUID.fromString(id))
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }

    /**
     * Массовое обновление позиций задач (для реордеринга или перемещения).
     * Позиции рассчитываются по порядку элементов в переданном списке
     * (значения `position` из доменной сущности игнорируются и перезаписываются индексом).
     * Также обновляется принадлежность к колонке, что позволяет выполнять перемещение между колонками.
     * @param tasks задачи с идентификаторами и принадлежностью к проекту/колонке
     */
    override suspend fun updatePositions(tasks: List<Task>) {
        if (tasks.isEmpty()) return
        val z = ZoneId.systemDefault()
        tasks.forEachIndexed { index, task ->
            db
                .sql(
                    """
                    UPDATE tasks SET
                        project_id = :projectId, column_id = :columnId,
                        position = :position, updated_at = :updatedAt
                    WHERE id = :id
                    """,
                ).bind("id", UUID.fromString(task.id.value))
                .bind("projectId", UUID.fromString(task.projectId.value))
                .bind("columnId", UUID.fromString(task.columnId.value))
                .bind("position", index)
                .bind("updatedAt", LocalDateTime.now(z))
                .fetch()
                .rowsUpdated()
                .awaitSingle()
        }
    }

    /**
     * Архивирование задачи — установка флага `archived = TRUE` и обновление `updated_at`.
     * @param id идентификатор задачи
     */
    override suspend fun archive(id: String) {
        db
            .sql("UPDATE tasks SET archived = TRUE, updated_at = :updatedAt WHERE id = :id")
            .bind("id", UUID.fromString(id))
            .bind("updatedAt", LocalDateTime.now())
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }

    /**
     * Преобразование строки результата запроса R2DBC в доменную сущность [Task].
     * @param row строка результата запроса
     * @return доменная сущность [Task]
     */
    private fun io.r2dbc.spi.Row.toTask(): Task {
        val table =
            TaskTable(
                id = get("id", String::class.java)!!,
                projectId = get("project_id", String::class.java)!!,
                columnId = get("column_id", String::class.java)!!,
                title = get("title", String::class.java)!!,
                description = get("description", String::class.java),
                assigneeId = get("assignee_id", String::class.java),
                position = get("position", java.lang.Integer::class.java)!!.toInt(),
                dueDate = get("due_date", java.time.LocalDateTime::class.java),
                priority = get("priority", String::class.java),
                archived = get("archived", java.lang.Boolean::class.java)!!.booleanValue(),
                createdAt = get("created_at", java.time.LocalDateTime::class.java)!!,
                updatedAt = get("updated_at", java.time.LocalDateTime::class.java)!!,
            )
        return table.toDomain()
    }

    private data class ArchivedRow(
        val archived: Boolean,
    )
}
