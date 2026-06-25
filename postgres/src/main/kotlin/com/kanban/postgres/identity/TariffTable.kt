package com.kanban.postgres.identity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

/**
 * Таблица `tariffs` — хранит доступные тарифные планы.
 * Каждый тариф определяет лимиты: максимальное количество проектов, досок на проект,
 * задач на доску, а также ограничения по размеру файлов и общему объёму хранилища.
 */
@Table("tariffs")
internal data class TariffTable(
    @Id
    val id: String,
    @Column("name")
    val name: String,
    @Column("max_projects")
    val maxProjects: Int,
    @Column("max_boards_per_project")
    val maxBoardsPerProject: Int,
    @Column("max_tasks_per_board")
    val maxTasksPerBoard: Int,
    @Column("max_file_size_mb")
    val maxFileSizeMb: Int,
    @Column("max_storage_mb")
    val maxStorageMb: Int,
    @Column("created_at")
    val createdAt: java.time.LocalDateTime,
)
