package com.kanban.postgres.report

import com.kanban.report.CfdDataPoint
import com.kanban.report.Interval
import com.kanban.report.LeadTimeDataPoint
import com.kanban.report.ReportCriteria
import com.kanban.report.ReportRepository
import java.time.DayOfWeek
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.util.UUID
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository

@Repository
internal class ReportRepositoryImpl(
    private val db: DatabaseClient,
) : ReportRepository {
    override suspend fun getCfd(criteria: ReportCriteria): List<CfdDataPoint> {
        val z = ZoneId.systemDefault()
        val sql = buildCfdSql(criteria)
        val rows =
            db
                .sql(sql)
                .let { bindParams(it, criteria) }
                .bind("fromDate", criteria.fromDate.atZone(z).toLocalDateTime())
                .bind("toDate", criteria.toDate.atZone(z).toLocalDateTime())
                .map { row, _ ->
                    CfdRaw(
                        columnId = row.get("column_id", String::class.java)!!,
                        columnName = row.get("column_name", String::class.java)!!,
                        createdAt = row.get("created_at", LocalDateTime::class.java)!!,
                    )
                }.all()
                .collectList()
                .awaitSingle()
        return bucketAndCount(rows, criteria.interval, z)
    }

    override suspend fun getLeadTime(criteria: ReportCriteria): List<LeadTimeDataPoint> {
        val z = ZoneId.systemDefault()
        val sql = buildLeadTimeSql(criteria)
        return db
            .sql(sql)
            .let { bindParams(it, criteria) }
            .bind("fromDate", criteria.fromDate.atZone(z).toLocalDateTime())
            .bind("toDate", criteria.toDate.atZone(z).toLocalDateTime())
            .map { row, _ ->
                val createdAt = row.get("created_at", LocalDateTime::class.java)!!
                val updatedAt = row.get("updated_at", LocalDateTime::class.java)!!
                val hours = Duration.between(createdAt, updatedAt).toSeconds() / 3600.0
                LeadTimeDataPoint(
                    date = createdAt.atZone(z).toInstant(),
                    taskId = row.get("id", String::class.java)!!,
                    taskTitle = row.get("title", String::class.java)!!,
                    leadTimeHours = hours,
                )
            }.all()
            .collectList()
            .awaitSingle()
    }

    private fun buildCfdSql(criteria: ReportCriteria): String {
        val sb = StringBuilder()
        sb.append("SELECT t.column_id, c.name AS column_name, t.created_at FROM tasks t")
        sb.append(" JOIN columns c ON t.column_id = c.id")
        sb.append(" WHERE t.archived = FALSE AND t.created_at >= :fromDate AND t.created_at <= :toDate")
        if (criteria.projectId != null) {
            sb.append(" AND t.project_id = :projectId")
        }
        sb.append(" ORDER BY t.created_at")
        return sb.toString()
    }

    private fun buildLeadTimeSql(criteria: ReportCriteria): String {
        val sb = StringBuilder()
        sb.append("SELECT t.id, t.title, t.created_at, t.updated_at FROM tasks t")
        sb.append(" WHERE t.archived = TRUE AND t.created_at >= :fromDate AND t.created_at <= :toDate")
        if (criteria.projectId != null) {
            sb.append(" AND t.project_id = :projectId")
        }
        sb.append(" ORDER BY t.created_at")
        return sb.toString()
    }

    private fun bindParams(
        spec: DatabaseClient.GenericExecuteSpec,
        criteria: ReportCriteria,
    ): DatabaseClient.GenericExecuteSpec {
        var s = spec
        criteria.projectId?.let { s = s.bind("projectId", UUID.fromString(it)) }
        return s
    }

    private fun bucketAndCount(
        rows: List<CfdRaw>,
        interval: Interval,
        zone: ZoneId,
    ): List<CfdDataPoint> {
        val bucketed =
            rows.groupBy { row ->
                val bucketStart =
                    bucketInstant(
                        row.createdAt.atZone(zone).toInstant(),
                        interval,
                        zone,
                    )
                Pair(bucketStart, row.columnId)
            }
        return bucketed
            .map { (key, group) ->
                CfdDataPoint(
                    date = key.first,
                    columnId = key.second,
                    columnName = group.first().columnName,
                    count = group.size.toLong(),
                )
            }.sortedWith(compareBy({ it.date }, { it.columnId }))
    }

    private fun bucketInstant(
        instant: Instant,
        interval: Interval,
        zone: ZoneId,
    ): Instant {
        val localDate = instant.atZone(zone).toLocalDate()
        return when (interval) {
            Interval.DAY -> localDate.atStartOfDay(zone).toInstant()
            Interval.WEEK ->
                localDate
                    .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                    .atStartOfDay(zone)
                    .toInstant()
            Interval.MONTH -> localDate.withDayOfMonth(1).atStartOfDay(zone).toInstant()
        }
    }

    private data class CfdRaw(
        val columnId: String,
        val columnName: String,
        val createdAt: LocalDateTime,
    )
}
