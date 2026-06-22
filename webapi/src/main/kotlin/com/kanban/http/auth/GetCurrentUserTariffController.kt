package com.kanban.http.auth

import com.fasterxml.jackson.annotation.JsonProperty
import com.kanban.identity.ProfileHandler
import com.kanban.identity.TariffResult
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/profile")
internal class GetCurrentUserTariffController(
    private val handler: ProfileHandler,
) {
    @GetMapping("/tariff")
    suspend fun getTariff(@RequestParam("user_id") userId: String): ResponseEntity<TariffResponse> =
        when (val result = handler.getTariff(userId)) {
            is TariffResult.Success -> ResponseEntity.ok(result.toResponse())
            is TariffResult.NotFound -> ResponseEntity.notFound().build()
        }
}

internal data class TariffResponse(
    val name: String,
    @JsonProperty("max_projects")
    val maxProjects: Int,
    @JsonProperty("max_boards_per_project")
    val maxBoardsPerProject: Int,
    @JsonProperty("max_tasks_per_board")
    val maxTasksPerBoard: Int,
    @JsonProperty("max_file_size_mb")
    val maxFileSizeMb: Int,
    @JsonProperty("max_storage_mb")
    val maxStorageMb: Int,
)

internal fun TariffResult.Success.toResponse(): TariffResponse {
    val t = tariff
    return TariffResponse(
        name = t.name,
        maxProjects = t.maxProjects,
        maxBoardsPerProject = t.maxBoardsPerProject,
        maxTasksPerBoard = t.maxTasksPerBoard,
        maxFileSizeMb = t.maxFileSizeMb,
        maxStorageMb = t.maxStorageMb,
    )
}
