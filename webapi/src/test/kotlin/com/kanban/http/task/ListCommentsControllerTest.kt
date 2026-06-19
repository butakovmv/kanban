package com.kanban.http.task

import com.kanban.common.CommentId
import com.kanban.common.TaskId
import com.kanban.task.Comment
import com.kanban.task.ListCommentsOperation
import io.mockk.coEvery
import java.time.Instant
import java.util.UUID
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

/**
 * Тесты контроллера получения списка комментариев.
 * Проверяют корректность кодов ответа и тел запросов/ответов.
 */
internal class ListCommentsControllerTest : BaseTaskControllerTest() {
    private lateinit var webClient: WebTestClient

    @BeforeEach
    fun setUp() {
        webClient = bindTo(ListCommentsController::class.java)
    }

    @Test
    fun `should return 200 with comments list`() {
        val taskId = "task-${UUID.randomUUID()}"
        val comments =
            listOf(
                Comment(
                    id = CommentId("comment-1"),
                    taskId = TaskId(taskId),
                    authorId = "user-1",
                    text = "First",
                    createdAt = Instant.now(),
                    updatedAt = Instant.now(),
                ),
                Comment(
                    id = CommentId("comment-2"),
                    taskId = TaskId(taskId),
                    authorId = "user-2",
                    text = "Second",
                    createdAt = Instant.now(),
                    updatedAt = Instant.now(),
                ),
            )

        coEvery {
            listCommentsOperation.execute(any())
        } returns ListCommentsOperation.Result.Success(comments = comments)

        webClient
            .get()
            .uri("/api/v1/tasks/$taskId/comments")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.comments.length()")
            .isEqualTo(2)
            .jsonPath("$.comments[0].id")
            .isEqualTo("comment-1")
            .jsonPath("$.comments[0].task_id")
            .isEqualTo(taskId)
            .jsonPath("$.comments[0].text")
            .isEqualTo("First")
            .jsonPath("$.comments[1].id")
            .isEqualTo("comment-2")
    }

    @Test
    fun `should return 200 with empty list when no comments`() {
        val taskId = "task-${UUID.randomUUID()}"

        coEvery {
            listCommentsOperation.execute(any())
        } returns ListCommentsOperation.Result.Success(comments = emptyList())

        webClient
            .get()
            .uri("/api/v1/tasks/$taskId/comments")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.comments.length()")
            .isEqualTo(0)
    }
}
