package com.kanban.postgres.project

import com.kanban.project.Board
import com.kanban.project.BoardRepository
import java.time.LocalDateTime
import java.time.ZoneId
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository

/**
 * Реализация [BoardRepository] через R2DBC и DatabaseClient.
 * Реализует паттерн Upsert: при сохранении проверяет существование доски
 * и выполняет INSERT или UPDATE соответственно, сохраняя признак архивирования.
 */
@Repository
internal class BoardRepositoryImpl(
    private val db: DatabaseClient,
) : BoardRepository {
    /**
     * Сохранение доски (создание или обновление).
     * При обновлении сохраняется текущий признак архивирования из БД.
     * @param board доменная сущность доски
     * @return сохранённая доска
     */
    override suspend fun save(board: Board): Board {
        val z = ZoneId.systemDefault()
        val createdAt = board.createdAt.atZone(z).toLocalDateTime()
        val existing = findRawById(board.id.value)
        if (existing != null) {
            updateBoard(board, existing.archived)
        } else {
            insertBoard(board, createdAt)
        }
        return board
    }

    /**
     * Обновление существующей записи доски в таблице `boards`.
     * Сохраняет ранее установленный флаг `archived`.
     * @param board доменная сущность доски с обновлёнными данными
     * @param archived текущий признак архивирования записи
     */
    private suspend fun updateBoard(
        board: Board,
        archived: Boolean,
    ) {
        db
            .sql(
                """
                UPDATE boards SET
                    project_id = :projectId, name = :name,
                    position = :position, archived = :archived
                WHERE id = :id
                """,
            ).bind("id", board.id.value)
            .bind("projectId", board.projectId.value)
            .bind("name", board.name)
            .bind("position", board.position)
            .bind("archived", archived)
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }

    /**
     * Вставка новой записи доски в таблицу `boards`.
     * @param board доменная сущность доски для сохранения
     * @param createdAt метка времени создания
     */
    private suspend fun insertBoard(
        board: Board,
        createdAt: LocalDateTime,
    ) {
        db
            .sql(
                """
                INSERT INTO boards (id, project_id, name, position, archived, created_at)
                VALUES (:id, :projectId, :name, :position, :archived, :createdAt)
                """,
            ).bind("id", board.id.value)
            .bind("projectId", board.projectId.value)
            .bind("name", board.name)
            .bind("position", board.position)
            .bind("archived", false)
            .bind("createdAt", createdAt)
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }

    /**
     * Поиск доски по идентификатору.
     * @param id строковый идентификатор доски
     * @return [Board] или null, если доска не найдена
     */
    override suspend fun findById(id: String): Board? =
        db
            .sql("SELECT * FROM boards WHERE id = :id")
            .bind("id", id)
            .map { row, _ -> row.toBoard() }
            .one()
            .awaitFirstOrNull()

    /**
     * Чтение сырой записи доски из БД с сохранением признака `archived`.
     * @param id строковый идентификатор доски
     * @return [BoardTable] или null, если запись не найдена
     */
    private suspend fun findRawById(id: String): BoardTable? =
        db
            .sql("SELECT * FROM boards WHERE id = :id")
            .bind("id", id)
            .map { row, _ ->
                BoardTable(
                    id = row.get("id", String::class.java)!!,
                    projectId = row.get("project_id", String::class.java)!!,
                    name = row.get("name", String::class.java)!!,
                    position = row.get("position", java.lang.Integer::class.java)!!.toInt(),
                    archived = row.get("archived", java.lang.Boolean::class.java)!!.booleanValue(),
                    createdAt = row.get("created_at", java.time.LocalDateTime::class.java)!!,
                )
            }.one()
            .awaitFirstOrNull()

    /**
     * Получение списка досок проекта, упорядоченных по позиции.
     * @param projectId идентификатор проекта
     * @return список [Board] проекта
     */
    override suspend fun listByProjectId(projectId: String): List<Board> =
        db
            .sql("SELECT * FROM boards WHERE project_id = :projectId ORDER BY position")
            .bind("projectId", projectId)
            .map { row, _ -> row.toBoard() }
            .all()
            .collectList()
            .awaitSingle()

    /**
     * Удаление доски по идентификатору.
     * @param id идентификатор доски
     */
    override suspend fun delete(id: String) {
        db
            .sql("DELETE FROM boards WHERE id = :id")
            .bind("id", id)
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }

    /**
     * Архивирование доски — установка флага `archived = TRUE`.
     * @param id идентификатор доски
     */
    override suspend fun archive(id: String) {
        db
            .sql("UPDATE boards SET archived = TRUE WHERE id = :id")
            .bind("id", id)
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }

    /**
     * Преобразование строки результата запроса R2DBC в доменную сущность [Board].
     * Поле `archived` намеренно отбрасывается при маппинге в домен.
     * @param row строка результата запроса
     * @return доменная сущность [Board]
     */
    private fun io.r2dbc.spi.Row.toBoard(): Board {
        val table =
            BoardTable(
                id = get("id", String::class.java)!!,
                projectId = get("project_id", String::class.java)!!,
                name = get("name", String::class.java)!!,
                position = get("position", java.lang.Integer::class.java)!!.toInt(),
                archived = get("archived", java.lang.Boolean::class.java)!!.booleanValue(),
                createdAt = get("created_at", java.time.LocalDateTime::class.java)!!,
            )
        return table.toDomain()
    }
}
