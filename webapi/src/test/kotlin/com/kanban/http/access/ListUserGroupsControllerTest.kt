package com.kanban.http.access

import com.kanban.access.Group
import com.kanban.access.ListUserGroupsOperation
import com.kanban.common.GroupId
import io.mockk.coEvery
import java.time.Instant
import java.util.UUID
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

internal class ListUserGroupsControllerTest : BaseAccessControllerTest() {
    private lateinit var webClient: WebTestClient

    @BeforeEach
    fun setUp() {
        webClient = bindTo(ListUserGroupsController::class.java)
    }

    @Test
    fun `should return 200 with groups list for user`() {
        val userId = "user-${UUID.randomUUID()}"
        val groups =
            listOf(
                Group(
                    id = GroupId("g-1"),
                    name = "Group 1",
                    description = null,
                    createdAt = Instant.now(),
                ),
            )

        coEvery {
            listUserGroupsOperation.execute(any())
        } returns ListUserGroupsOperation.Result.Success(groups = groups)

        webClient
            .get()
            .uri("/api/v1/users/$userId/groups")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.groups.length()")
            .isEqualTo(1)
            .jsonPath("$.groups[0].id")
            .isEqualTo("g-1")
            .jsonPath("$.groups[0].name")
            .isEqualTo("Group 1")
    }

    @Test
    fun `should return 200 with empty list when user has no groups`() {
        val userId = "user-${UUID.randomUUID()}"

        coEvery {
            listUserGroupsOperation.execute(any())
        } returns ListUserGroupsOperation.Result.Success(groups = emptyList())

        webClient
            .get()
            .uri("/api/v1/users/$userId/groups")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.groups.length()")
            .isEqualTo(0)
    }
}
