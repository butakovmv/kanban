package com.kanban.postgres.project

import com.kanban.project.Column
import com.kanban.project.ColumnRepository
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository

/**
 * Реализация [ColumnRepository] через R2DBC и DatabaseClient.
 * Реализует паттерн Upsert: при сохранении проверяет существование колонки
 * и выполняет INSERT или UPDATE соответственно.
 */
@Repository
internal class ColumnRepositoryImpl(
    private val db: DatabaseClient,
) : ColumnRepository {
    /**
     * Сохранение колонки (создание или обновление).
     * @param column доменная сущность колонки
     * @return сохранённая колонка
     */
    override suspend fun save(column: Column): Column {
        val z = ZoneId.systemDefault()
        val createdAt = column.createdAt.atZone(z).toLocalDateTime()
        if (findById(column.id.value) != null) {
            updateColumn(column)
        } else {
            insertColumn(column, createdAt)
        }
        return column
    }

    /**
     * Обновление существующей записи колонки в таблице `columns`.
     * @param column доменная сущность колонки с обновлёнными данными
     */
    private suspend fun updateColumn(column: Column) {
        db
            .sql(
                """
                UPDATE columns SET
                    board_id = :boardId, name = :name,
                    position = :position, wip_limit = :wipLimit
                WHERE id = :id
                """,
            ).bind("id", UUID.fromString(column.id.value))
            .bind("boardId", UUID.fromString(column.boardId.value))
            .bind("name", column.name)
            .bind("position", column.position)
            .let { spec ->
                val wipLimit = column.wipLimit
                if (wipLimit != null) {
                    spec.bind("wipLimit", wipLimit)
                } else {
                    spec.bindNull("wipLimit", Integer::class.java)
                }
            }.fetch()
            .rowsUpdated()
            .awaitSingle()
    }

    /**
     * Вставка новой записи колонки в таблицу `columns`.
     * @param column доменная сущность колонки для сохранения
     * @param createdAt метка времени создания
     */
    private suspend fun insertColumn(
        column: Column,
        createdAt: LocalDateTime,
    ) {
        db
            .sql(
                """
                INSERT INTO columns (id, board_id, name, position, wip_limit, created_at)
                VALUES (:id, :boardId, :name, :position, :wipLimit, :createdAt)
                """,
            ).bind("id", UUID.fromString(column.id.value))
            .bind("boardId", UUID.fromString(column.boardId.value))
            .bind("name", column.name)
            .bind("position", column.position)
            .let { spec ->
                val wipLimit = column.wipLimit
                if (wipLimit != null) {
                    spec.bind("wipLimit", wipLimit)
                } else {
                    spec.bindNull("wipLimit", Integer::class.java)
                }
            }.bind("createdAt", createdAt)
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }

    /**
     * Поиск колонки по идентификатору.
     * @param id строковый идентификатор колонки
     * @return [Column] или null, если колонка не найдена
     */
    override suspend fun findById(id: String): Column? =
        db
            .sql("SELECT * FROM columns WHERE id = :id")
            .bind("id", UUID.fromString(id))
            .map { row, _ -> row.toColumn() }
            .one()
            .awaitFirstOrNull()

    /**
     * Получение списка колонок указанной доски, упорядоченных по позиции.
     * @param boardId идентификатор доски
     * @return список [Column] доски
     */
    override suspend fun listByBoardId(boardId: String): List<Column> =
        db
            .sql("SELECT * FROM columns WHERE board_id = :boardId ORDER BY position")
            .bind("boardId", UUID.fromString(boardId))
            .map { row, _ -> row.toColumn() }
            .all()
            .collectList()
            .awaitSingle()

    /**
     * Удаление колонки по идентификатору.
     * @param id идентификатор колонки
     */
    override suspend fun delete(id: String) {
        db
            .sql("DELETE FROM columns WHERE id = :id")
            .bind("id", UUID.fromString(id))
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }

    /**
     * Массовое обновление позиций колонок (для реордеринга).
     * Позиции рассчитываются по порядку элементов в переданном списке
     * (значения `position` из доменной сущности игнорируются и перезаписываются индексом).
     * @param columns колонки с идентификаторами и принадлежностью к доске
     */
    override suspend fun updatePositions(columns: List<Column>) {
        if (columns.isEmpty()) return
        val z = ZoneId.systemDefault()
        columns.forEachIndexed { index, column ->
            db
                .sql(
                    """
                    UPDATE columns SET
                        board_id = :boardId, name = :name,
                        position = :position, wip_limit = :wipLimit
                    WHERE id = :id
                    """,
                ).bind("id", UUID.fromString(column.id.value))
                .bind("boardId", UUID.fromString(column.boardId.value))
                .bind("name", column.name)
                .bind("position", index)
                .let { spec ->
                    val wipLimit = column.wipLimit
                    if (wipLimit != null) {
                        spec.bind("wipLimit", wipLimit)
                    } else {
                        spec.bindNull("wipLimit", Integer::class.java)
                    }
                }.fetch()
                .rowsUpdated()
                .awaitSingle()
        }
    }

    /**
     * Преобразование строки результата запроса R2DBC в доменную сущность [Column].
     * @param row строка результата запроса
     * @return доменная сущность [Column]
     */
    private fun io.r2dbc.spi.Row.toColumn(): Column {
        val table =
            ColumnTable(
                id = get("id", String::class.java)!!,
                boardId = get("board_id", String::class.java)!!,
                name = get("name", String::class.java)!!,
                position = get("position", java.lang.Integer::class.java)!!.toInt(),
                wipLimit = get("wip_limit", java.lang.Integer::class.java)?.toInt(),
                createdAt = get("created_at", java.time.LocalDateTime::class.java)!!,
            )
        return table.toDomain()
    }
}
