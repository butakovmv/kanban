package com.kanban.http.task

import com.kanban.common.CommentId
import com.kanban.common.TaskId
import com.kanban.task.Comment
import com.kanban.task.UpdateCommentOperation
import io.mockk.coEvery
import io.mockk.coVerify
import java.time.Instant
import java.util.UUID
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

/**
 * Тесты контроллера обновления комментария.
 * Проверяют корректность кодов ответа и тел запросов/ответов.
 */
internal class UpdateCommentControllerTest : BaseTaskControllerTest() {
    private lateinit var webClient: WebTestClient

    @BeforeEach
    fun setUp() {
        webClient = bindTo(UpdateCommentController::class.java)
    }

    @Test
    fun `should update comment and return 200`() {
        val id = "comment-${UUID.randomUUID()}"
        val body = RequestGenerator.updateCommentBody()
        val comment =
            Comment(
                id = CommentId(id),
                taskId = TaskId("task-1"),
                authorId = "user-1",
                text = body.text,
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
            )

        coEvery {
            updateCommentOperation.execute(any())
        } returns UpdateCommentOperation.Result.Success(comment = comment)

        webClient
            .put()
            .uri("/api/v1/comments/$id")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.id")
            .isEqualTo(id)
            .jsonPath("$.text")
            .isEqualTo(body.text)

        coVerify {
            updateCommentOperation.execute(match { it.commentId == id && it.text == body.text })
        }
    }

    @Test
    fun `should return 404 when comment not found`() {
        val id = "missing-${UUID.randomUUID()}"
        val body = RequestGenerator.updateCommentBody()

        coEvery {
            updateCommentOperation.execute(any())
        } returns UpdateCommentOperation.Result.NotFound

        webClient
            .put()
            .uri("/api/v1/comments/$id")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            .expectStatus()
            .isNotFound
    }

    @Test
    fun `should return 400 on failure`() {
        val id = "comment-${UUID.randomUUID()}"
        val body = RequestGenerator.updateCommentBody()

        coEvery {
            updateCommentOperation.execute(any())
        } returns UpdateCommentOperation.Result.Failure("Text cannot be empty")

        webClient
            .put()
            .uri("/api/v1/comments/$id")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            .expectStatus()
            .isBadRequest
            .expectBody()
            .jsonPath("$.reason")
            .isEqualTo("Text cannot be empty")
    }
}
