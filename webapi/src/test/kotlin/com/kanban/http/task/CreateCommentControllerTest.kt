package com.kanban.http.task

import com.kanban.common.CommentId
import com.kanban.common.TaskId
import com.kanban.task.Comment
import com.kanban.task.CreateCommentOperation
import io.mockk.coEvery
import java.time.Instant
import java.util.UUID
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

/**
 * Тесты контроллера создания комментария.
 * Проверяют корректность кодов ответа и тел запросов/ответов.
 */
internal class CreateCommentControllerTest : BaseTaskControllerTest() {
    private lateinit var webClient: WebTestClient

    @BeforeEach
    fun setUp() {
        webClient = bindTo(CreateCommentController::class.java)
    }

    @Test
    fun `should create comment and return 201`() {
        val taskId = "task-${UUID.randomUUID()}"
        val body = RequestGenerator.createCommentBody()
        val comment =
            Comment(
                id = CommentId("new-comment-id"),
                taskId = TaskId(taskId),
                authorId = body.authorId,
                text = body.text,
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
            )

        coEvery {
            createCommentOperation.execute(any())
        } returns CreateCommentOperation.Result.Success(comment = comment)

        webClient
            .post()
            .uri("/api/v1/tasks/$taskId/comments")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            .expectStatus()
            .isCreated
            .expectBody()
            .jsonPath("$.id")
            .isEqualTo("new-comment-id")
            .jsonPath("$.task_id")
            .isEqualTo(taskId)
            .jsonPath("$.author_id")
            .isEqualTo(body.authorId)
            .jsonPath("$.text")
            .isEqualTo(body.text)
    }

    @Test
    fun `should return 400 on failure`() {
        val taskId = "task-${UUID.randomUUID()}"
        val body = RequestGenerator.createCommentBody()

        coEvery {
            createCommentOperation.execute(any())
        } returns CreateCommentOperation.Result.Failure("Task not found")

        webClient
            .post()
            .uri("/api/v1/tasks/$taskId/comments")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            .expectStatus()
            .isBadRequest
            .expectBody()
            .jsonPath("$.reason")
            .isEqualTo("Task not found")
    }
}
