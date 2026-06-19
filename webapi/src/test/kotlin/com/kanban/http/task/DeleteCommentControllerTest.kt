package com.kanban.http.task

import com.kanban.task.DeleteCommentOperation
import io.mockk.coEvery
import io.mockk.coVerify
import java.util.UUID
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.web.reactive.server.WebTestClient

/**
 * Тесты контроллера удаления комментария.
 * Проверяют корректность кодов ответа.
 */
internal class DeleteCommentControllerTest : BaseTaskControllerTest() {
    private lateinit var webClient: WebTestClient

    @BeforeEach
    fun setUp() {
        webClient = bindTo(DeleteCommentController::class.java)
    }

    @Test
    fun `should delete comment and return 204`() {
        val id = "comment-${UUID.randomUUID()}"

        coEvery {
            deleteCommentOperation.execute(any())
        } returns DeleteCommentOperation.Result.Success

        webClient
            .delete()
            .uri("/api/v1/comments/$id")
            .exchange()
            .expectStatus()
            .isNoContent

        coVerify {
            deleteCommentOperation.execute(match { it.commentId == id })
        }
    }

    @Test
    fun `should return 404 when comment not found`() {
        val id = "missing-${UUID.randomUUID()}"

        coEvery {
            deleteCommentOperation.execute(any())
        } returns DeleteCommentOperation.Result.NotFound

        webClient
            .delete()
            .uri("/api/v1/comments/$id")
            .exchange()
            .expectStatus()
            .isNotFound
    }
}
