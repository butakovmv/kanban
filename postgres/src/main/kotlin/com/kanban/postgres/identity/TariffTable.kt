package com.kanban.postgres.identity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("tariffs")
internal data class TariffTable(
    @Id
    val id: String,
    val name: String,
    val maxProjects: Int,
    val maxBoardsPerProject: Int,
    val maxTasksPerBoard: Int,
    val maxFileSizeMb: Int,
    val maxStorageMb: Int,
    val createdAt: java.time.LocalDateTime,
)
