package com.kanban.identity

data class TariffLimits(
    val maxProjects: Int,
    val maxBoardsPerProject: Int,
    val maxTasksPerBoard: Int,
    val maxFileSizeMb: Int,
    val maxStorageMb: Int,
) {
    init {
        require(maxProjects >= 1) { "maxProjects must be >= 1" }
        require(maxBoardsPerProject >= 1) { "maxBoardsPerProject must be >= 1" }
        require(maxTasksPerBoard >= 1) { "maxTasksPerBoard must be >= 1" }
        require(maxFileSizeMb >= 1) { "maxFileSizeMb must be >= 1" }
        require(maxStorageMb >= 1) { "maxStorageMb must be >= 1" }
    }
}
