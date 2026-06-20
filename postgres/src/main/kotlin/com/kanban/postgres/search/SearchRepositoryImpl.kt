package com.kanban.postgres.search

import com.kanban.search.SearchCriteria
import com.kanban.search.SearchRepository
import com.kanban.search.SearchResult
import java.time.LocalDateTime
import java.time.ZoneId
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository

@Repository
internal class SearchRepositoryImpl(
    private val db: DatabaseClient,
) : SearchRepository {
    override suspend fun search(criteria: SearchCriteria): List<SearchResult> {
        val z = ZoneId.systemDefault()
        var spec = db.sql(buildSearchQuery(criteria))
        spec = bindCommon(spec, criteria, z)
        spec = spec.bind("limit", criteria.limit.toLong())
        spec = spec.bind("offset", criteria.offset.toLong())
        return spec
            .map { row, _ ->
                SearchResult(
                    id = row.get("id", String::class.java)!!,
                    title = row.get("title", String::class.java)!!,
                    description = row.get("description", String::class.java),
                    status = row.get("status", String::class.java) ?: "",
                    priority = row.get("priority", String::class.java),
                    assigneeId = row.get("assignee_id", String::class.java),
                    boardId = row.get("board_id", String::class.java)!!,
                    columnId = row.get("column_id", String::class.java)!!,
                    projectId = row.get("project_id", String::class.java)!!,
                    dueDate = row.get("due_date", LocalDateTime::class.java)?.atZone(z)?.toInstant(),
                    createdAt = row.get("created_at", LocalDateTime::class.java)!!.atZone(z).toInstant(),
                    updatedAt = row.get("updated_at", LocalDateTime::class.java)!!.atZone(z).toInstant(),
                    rank = row.get("rank", java.lang.Float::class.java)?.toFloat() ?: 0f,
                )
            }.all()
            .collectList()
            .awaitSingle()
    }

    override suspend fun count(criteria: SearchCriteria): Long {
        val z = ZoneId.systemDefault()
        val spec = bindCommon(db.sql(buildCountQuery(criteria)), criteria, z)
        return spec
            .map { row, _ -> (row.get("cnt", java.lang.Long::class.java) ?: 0L) as Long }
            .one()
            .awaitSingle()
    }

    private fun buildSearchQuery(criteria: SearchCriteria): String =
        """
        SELECT
            t.id, t.title, t.description,
            '' AS status,
            NULL AS priority,
            t.assignee_id, t.board_id, t.column_id,
            b.project_id,
            t.due_date, t.created_at, t.updated_at,
            ts_rank(t.search_vector, plainto_tsquery('russian', :query)) AS rank
        FROM tasks t
        JOIN boards b ON b.id = t.board_id
        WHERE t.archived = FALSE
        """.trimIndent() +
            filterClauses(criteria) +
            "\nORDER BY " +
            (if (criteria.query != null) "rank DESC" else "t.created_at DESC") +
            "\nLIMIT :limit OFFSET :offset"

    private fun buildCountQuery(criteria: SearchCriteria): String =
        """
        SELECT COUNT(*) AS cnt
        FROM tasks t
        JOIN boards b ON b.id = t.board_id
        WHERE t.archived = FALSE
        """.trimIndent() +
            filterClauses(criteria)

    private fun filterClauses(criteria: SearchCriteria): String {
        val clauses = mutableListOf<String>()
        if (criteria.query != null) {
            clauses.add("t.search_vector @@ plainto_tsquery('russian', :query)")
        }
        if (criteria.projectId != null) {
            clauses.add("b.project_id = :projectId")
        }
        if (criteria.boardId != null) {
            clauses.add("t.board_id = :boardId")
        }
        if (criteria.assigneeId != null) {
            clauses.add("t.assignee_id = :assigneeId")
        }
        if (criteria.dueDateFrom != null) {
            clauses.add("t.due_date >= :dueDateFrom")
        }
        if (criteria.dueDateTo != null) {
            clauses.add("t.due_date <= :dueDateTo")
        }
        return if (clauses.isEmpty()) "" else "\n" + clauses.joinToString("\n") { "    AND $it" }
    }

    private fun bindCommon(
        spec: DatabaseClient.GenericExecuteSpec,
        criteria: SearchCriteria,
        z: ZoneId,
    ): DatabaseClient.GenericExecuteSpec {
        var s = spec
        criteria.query?.let { s = s.bind("query", it) }
        criteria.projectId?.let { s = s.bind("projectId", it) }
        criteria.boardId?.let { s = s.bind("boardId", it) }
        criteria.assigneeId?.let { s = s.bind("assigneeId", it) }
        criteria.dueDateFrom?.let { s = s.bind("dueDateFrom", it.atZone(z).toLocalDateTime()) }
        criteria.dueDateTo?.let { s = s.bind("dueDateTo", it.atZone(z).toLocalDateTime()) }
        return s
    }
}
