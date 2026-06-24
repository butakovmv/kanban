package com.kanban.postgres.search

import com.kanban.search.SearchCriteria
import com.kanban.search.SearchRepository
import com.kanban.search.SearchResult
import java.time.LocalDateTime
import java.time.ZoneId
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.r2dbc.core.DatabaseClient

internal class TestSearchRepositoryImpl(
    private val db: DatabaseClient,
) : SearchRepository {
    override suspend fun search(criteria: SearchCriteria): List<SearchResult> {
        val z = ZoneId.systemDefault()
        val sql = buildSearchQuery(criteria)
        var spec = db.sql(sql)
        spec = bindCommon(spec, criteria, z)
        spec = spec.bind("limit", Integer.valueOf(criteria.limit))
        spec = spec.bind("offset", Integer.valueOf(criteria.offset))
        return spec
            .map { row, _ ->
                SearchResult(
                    id = row.get("id", String::class.java)!!,
                    title = row.get("title", String::class.java)!!,
                    description = row.get("description", String::class.java),
                    status = "",
                    priority = null,
                    assigneeId = row.get("assignee_id", String::class.java),
                    boardId = row.get("board_id", String::class.java)!!,
                    columnId = row.get("column_id", String::class.java)!!,
                    projectId = row.get("project_id", String::class.java)!!,
                    dueDate = row.get("due_date", LocalDateTime::class.java)?.atZone(z)?.toInstant(),
                    createdAt = row.get("created_at", LocalDateTime::class.java)!!.atZone(z).toInstant(),
                    updatedAt = row.get("updated_at", LocalDateTime::class.java)!!.atZone(z).toInstant(),
                    rank = 0f,
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

    private fun buildSearchQuery(criteria: SearchCriteria): String {
        val base =
            StringBuilder()
                .append("SELECT t.id, t.title, t.description, t.assignee_id, ")
                .append("t.column_id, t.project_id, b.id AS board_id, ")
                .append("t.due_date, t.created_at, t.updated_at ")
                .append("FROM tasks t JOIN boards b ON t.project_id = b.project_id ")
                .append("WHERE t.archived = FALSE")
                .toString()
        val clauses = filterClauses(criteria)
        val order = "ORDER BY t.title"
        return "$base$clauses\n$order\nLIMIT :limit OFFSET :offset"
    }

    private fun buildCountQuery(criteria: SearchCriteria): String {
        val base =
            StringBuilder()
                .append("SELECT COUNT(*) AS cnt ")
                .append("FROM tasks t JOIN boards b ON t.project_id = b.project_id ")
                .append("WHERE t.archived = FALSE")
                .toString()
        val clauses = filterClauses(criteria)
        return "$base$clauses"
    }

    private fun filterClauses(criteria: SearchCriteria): String {
        val clauses = mutableListOf<String>()
        if (criteria.query != null) {
            clauses.add(
                "(LOWER(t.title) LIKE '%' || LOWER(:query) || '%' " +
                    "OR LOWER(COALESCE(t.description, '')) LIKE '%' || LOWER(:query) || '%')",
            )
        }
        if (criteria.projectId != null) {
            clauses.add("t.project_id = :projectId")
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
        return if (clauses.isEmpty()) "" else "\nAND " + clauses.joinToString("\nAND ")
    }

    private fun bindCommon(
        spec: DatabaseClient.GenericExecuteSpec,
        criteria: SearchCriteria,
        z: ZoneId,
    ): DatabaseClient.GenericExecuteSpec {
        var s = spec
        criteria.query?.let { s = s.bind("query", it) }
        criteria.projectId?.let { s = s.bind("projectId", it) }
        criteria.assigneeId?.let { s = s.bind("assigneeId", it) }
        criteria.dueDateFrom?.let { s = s.bind("dueDateFrom", it.atZone(z).toLocalDateTime()) }
        criteria.dueDateTo?.let { s = s.bind("dueDateTo", it.atZone(z).toLocalDateTime()) }
        return s
    }
}
