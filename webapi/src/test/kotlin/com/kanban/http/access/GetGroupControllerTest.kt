package com.kanban.http.access

import com.kanban.access.GetGroupOperation
import com.kanban.access.Group
import com.kanban.common.GroupId
import io.mockk.coEvery
import java.time.Instant
import java.util.UUID
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

internal class GetGroupControllerTest : BaseAccessControllerTest() {
    private lateinit var webClient: WebTestClient

    @BeforeEach
    fun setUp() {
        webClient = bindTo(GetGroupController::class.java)
    }

    @Test
    fun `should return 200 with group when found`() {
        val id = "group-${UUID.randomUUID()}"
        val group =
            Group(
                id = GroupId(id),
                name = "Test Group",
                description = "Test Description",
                createdAt = Instant.now(),
            )

        coEvery {
            getGroupOperation.execute(any())
        } returns GetGroupOperation.Result.Success(group = group)

        webClient
            .get()
            .uri("/api/v1/groups/$id")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.id")
            .isEqualTo(id)
            .jsonPath("$.name")
            .isEqualTo("Test Group")
            .jsonPath("$.description")
            .isEqualTo("Test Description")
    }

    @Test
    fun `should return 404 when group not found`() {
        val id = "missing-${UUID.randomUUID()}"

        coEvery {
            getGroupOperation.execute(any())
        } returns GetGroupOperation.Result.NotFound

        webClient
            .get()
            .uri("/api/v1/groups/$id")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound
    }
}
