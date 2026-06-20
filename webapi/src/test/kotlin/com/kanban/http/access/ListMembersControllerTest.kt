package com.kanban.http.access

import com.kanban.access.GroupMember
import com.kanban.access.ListMembersOperation
import com.kanban.common.GroupId
import io.mockk.coEvery
import java.time.Instant
import java.util.UUID
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

internal class ListMembersControllerTest : BaseAccessControllerTest() {
    private lateinit var webClient: WebTestClient

    @BeforeEach
    fun setUp() {
        webClient = bindTo(ListMembersController::class.java)
    }

    @Test
    fun `should return 200 with members list`() {
        val groupId = "group-${UUID.randomUUID()}"
        val members =
            listOf(
                GroupMember(
                    groupId = GroupId(groupId),
                    userId = "user-1",
                    addedAt = Instant.now(),
                ),
                GroupMember(
                    groupId = GroupId(groupId),
                    userId = "user-2",
                    addedAt = Instant.now(),
                ),
            )

        coEvery {
            listMembersOperation.execute(any())
        } returns ListMembersOperation.Result.Success(members = members)

        webClient
            .get()
            .uri("/api/v1/groups/$groupId/members")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.members.length()")
            .isEqualTo(2)
            .jsonPath("$.members[0].user_id")
            .isEqualTo("user-1")
            .jsonPath("$.members[1].user_id")
            .isEqualTo("user-2")
    }

    @Test
    fun `should return 200 with empty list when no members`() {
        val groupId = "group-${UUID.randomUUID()}"

        coEvery {
            listMembersOperation.execute(any())
        } returns ListMembersOperation.Result.Success(members = emptyList())

        webClient
            .get()
            .uri("/api/v1/groups/$groupId/members")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.members.length()")
            .isEqualTo(0)
    }
}
