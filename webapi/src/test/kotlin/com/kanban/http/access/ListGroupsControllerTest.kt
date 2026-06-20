package com.kanban.http.access

import com.kanban.access.Group
import com.kanban.access.ListGroupsOperation
import com.kanban.common.GroupId
import io.mockk.coEvery
import java.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

internal class ListGroupsControllerTest : BaseAccessControllerTest() {
    private lateinit var webClient: WebTestClient

    @BeforeEach
    fun setUp() {
        webClient = bindTo(ListGroupsController::class.java)
    }

    @Test
    fun `should return 200 with groups list`() {
        val groups =
            listOf(
                Group(
                    id = GroupId("g-1"),
                    name = "Group 1",
                    description = "Desc 1",
                    createdAt = Instant.now(),
                ),
                Group(
                    id = GroupId("g-2"),
                    name = "Group 2",
                    description = null,
                    createdAt = Instant.now(),
                ),
            )

        coEvery {
            listGroupsOperation.execute(any())
        } returns ListGroupsOperation.Result.Success(groups = groups)

        webClient
            .get()
            .uri("/api/v1/groups")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.groups.length()")
            .isEqualTo(2)
            .jsonPath("$.groups[0].id")
            .isEqualTo("g-1")
            .jsonPath("$.groups[0].name")
            .isEqualTo("Group 1")
            .jsonPath("$.groups[1].id")
            .isEqualTo("g-2")
    }

    @Test
    fun `should return 200 with empty list when no groups`() {
        coEvery {
            listGroupsOperation.execute(any())
        } returns ListGroupsOperation.Result.Success(groups = emptyList())

        webClient
            .get()
            .uri("/api/v1/groups")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.groups.length()")
            .isEqualTo(0)
    }
}
