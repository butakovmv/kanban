package com.kanban.http.auth

import com.kanban.identity.GetCurrentUserTariffOperation
import com.kanban.identity.ProfileHandler
import com.kanban.identity.TariffInfo
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

internal class GetCurrentUserTariffControllerTest {
    private lateinit var webClient: WebTestClient
    private val getCurrentUserTariffOperation = mockk<GetCurrentUserTariffOperation>()

    @BeforeEach
    fun setUp() {
        val handler = ProfileHandler(getCurrentUserTariffOperation)
        webClient = WebTestClient.bindToController(GetCurrentUserTariffController(handler)).build()
    }

    @Test
    fun `should return 200 with tariff info when user has active tariff`() {
        val tariffInfo =
            TariffInfo(
                name = "Free",
                maxProjects = 2,
                maxBoardsPerProject = 3,
                maxTasksPerBoard = 10,
                maxFileSizeMb = 5,
                maxStorageMb = 100,
            )
        coEvery {
            getCurrentUserTariffOperation.execute(any())
        } returns GetCurrentUserTariffOperation.Result.Success(tariffInfo)

        webClient
            .get()
            .uri("/api/v1/profile/tariff")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.name")
            .isEqualTo("Free")
            .jsonPath("$.max_projects")
            .isEqualTo(2)
            .jsonPath("$.max_boards_per_project")
            .isEqualTo(3)
            .jsonPath("$.max_tasks_per_board")
            .isEqualTo(10)
            .jsonPath("$.max_file_size_mb")
            .isEqualTo(5)
            .jsonPath("$.max_storage_mb")
            .isEqualTo(100)
    }

    @Test
    fun `should return 404 when user has no active tariff`() {
        coEvery {
            getCurrentUserTariffOperation.execute(any())
        } returns GetCurrentUserTariffOperation.Result.NotFound

        webClient
            .get()
            .uri("/api/v1/profile/tariff")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound
    }
}
