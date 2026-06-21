package com.kanban.postgres.task

import com.kanban.task.Comment
import com.kanban.task.CommentRepository
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository

/**
 * Реализация [CommentRepository] через R2DBC и DatabaseClient.
 * Реализует паттерн Upsert: при сохранении проверяет существование комментария
 * и выполняет INSERT или UPDATE соответственно.
 */
@Repository
internal class CommentRepositoryImpl(
    private val db: DatabaseClient,
) : CommentRepository {
    /**
     * Сохранение комментария (создание или обновление).
     * @param comment доменная сущность комментария
     * @return сохранённый комментарий
     */
    override suspend fun save(comment: Comment): Comment {
        val z = ZoneId.systemDefault()
        val createdAt = comment.createdAt.atZone(z).toLocalDateTime()
        val updatedAt = comment.updatedAt.atZone(z).toLocalDateTime()
        if (findById(comment.id.value) != null) {
            updateComment(comment, updatedAt)
        } else {
            insertComment(comment, createdAt, updatedAt)
        }
        return comment
    }

    /**
     * Обновление существующей записи комментария в таблице `comments`.
     * @param comment доменная сущность комментария с обновлёнными данными
     * @param updatedAt метка времени обновления
     */
    private suspend fun updateComment(
        comment: Comment,
        updatedAt: LocalDateTime,
    ) {
        db
            .sql(
                """
                UPDATE comments SET
                    task_id = :taskId, author_id = :authorId,
                    text = :text, updated_at = :updatedAt
                WHERE id = :id
                """,
            ).bind("id", UUID.fromString(comment.id.value))
            .bind("taskId", UUID.fromString(comment.taskId.value))
            .bind("authorId", UUID.fromString(comment.authorId))
            .bind("text", comment.text)
            .bind("updatedAt", updatedAt)
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }

    /**
     * Вставка новой записи комментария в таблицу `comments`.
     * @param comment доменная сущность комментария для сохранения
     * @param createdAt метка времени создания
     * @param updatedAt метка времени обновления
     */
    private suspend fun insertComment(
        comment: Comment,
        createdAt: LocalDateTime,
        updatedAt: LocalDateTime,
    ) {
        db
            .sql(
                """
                INSERT INTO comments (id, task_id, author_id, text, created_at, updated_at)
                VALUES (:id, :taskId, :authorId, :text, :createdAt, :updatedAt)
                """,
            ).bind("id", UUID.fromString(comment.id.value))
            .bind("taskId", UUID.fromString(comment.taskId.value))
            .bind("authorId", UUID.fromString(comment.authorId))
            .bind("text", comment.text)
            .bind("createdAt", createdAt)
            .bind("updatedAt", updatedAt)
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }

    /**
     * Поиск комментария по идентификатору.
     * @param id строковый идентификатор комментария
     * @return [Comment] или null, если комментарий не найден
     */
    override suspend fun findById(id: String): Comment? =
        db
            .sql("SELECT * FROM comments WHERE id = :id")
            .bind("id", UUID.fromString(id))
            .map { row, _ -> row.toComment() }
            .one()
            .awaitFirstOrNull()

    /**
     * Получение списка комментариев указанной задачи, упорядоченных по дате создания.
     * @param taskId идентификатор задачи
     * @return список [Comment] задачи
     */
    override suspend fun listByTaskId(taskId: String): List<Comment> =
        db
            .sql("SELECT * FROM comments WHERE task_id = :taskId ORDER BY created_at")
            .bind("taskId", UUID.fromString(taskId))
            .map { row, _ -> row.toComment() }
            .all()
            .collectList()
            .awaitSingle()

    /**
     * Удаление комментария по идентификатору.
     * @param id идентификатор комментария
     */
    override suspend fun delete(id: String) {
        db
            .sql("DELETE FROM comments WHERE id = :id")
            .bind("id", UUID.fromString(id))
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }

    /**
     * Преобразование строки результата запроса R2DBC в доменную сущность [Comment].
     * @param row строка результата запроса
     * @return доменная сущность [Comment]
     */
    private fun io.r2dbc.spi.Row.toComment(): Comment {
        val table =
            CommentTable(
                id = get("id", String::class.java)!!,
                taskId = get("task_id", String::class.java)!!,
                authorId = get("author_id", String::class.java)!!,
                text = get("text", String::class.java)!!,
                createdAt = get("created_at", java.time.LocalDateTime::class.java)!!,
                updatedAt = get("updated_at", java.time.LocalDateTime::class.java)!!,
            )
        return table.toDomain()
    }
}
