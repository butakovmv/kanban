package com.kanban.sse

import java.time.Instant
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.jupiter.api.Test

internal class SinkServiceTest {
    private val sinkService = SinkService()

    @Test
    fun `should deliver event to registered global sink`() =
        runTest {
            val sink = sinkService.register()
            launch {
                sinkService.emit(SseEvent("test", """{}""", null, null, Instant.now()))
            }
            val event = sink.asFlow().first()
            assertEquals("test", event.type)
        }

    @Test
    fun `should deliver event to board-specific sink`() =
        runTest {
            val sink = sinkService.register(boardId = "board-1")
            launch {
                sinkService.emit(
                    SseEvent("board_event", """{"board_id":"board-1"}""", "board-1", null, Instant.now()),
                )
            }
            val event = sink.asFlow().first()
            assertEquals("board_event", event.type)
        }

    @Test
    fun `should not deliver event to board sink for different board`() =
        runTest {
            val sink = sinkService.register(boardId = "board-2")
            launch {
                sinkService.emit(
                    SseEvent("board_event", """{"board_id":"board-1"}""", "board-1", null, Instant.now()),
                )
            }
            val event =
                withTimeoutOrNull(100) {
                    sink.asFlow().first()
                }
            assertEquals(null, event)
        }

    @Test
    fun `should deliver event to project-specific sink`() =
        runTest {
            val sink = sinkService.register(projectId = "project-1")
            launch {
                sinkService.emit(
                    SseEvent("project_event", """{"project_id":"project-1"}""", null, "project-1", Instant.now()),
                )
            }
            val event = sink.asFlow().first()
            assertEquals("project_event", event.type)
        }

    @Test
    fun `should stop delivering after unregister`() =
        runTest {
            val sink = sinkService.register()
            sinkService.unregister(sink)
            launch {
                sinkService.emit(SseEvent("test", """{}""", null, null, Instant.now()))
            }
            val result =
                withTimeoutOrNull(100) {
                    sink.asFlow().first()
                }
            assertEquals(null, result)
        }

    @Test
    fun `should deliver event to global sink even when board sink exists`() =
        runTest {
            val globalSink = sinkService.register()
            sinkService.register(boardId = "board-1")

            launch {
                sinkService.emit(
                    SseEvent("event", """{}""", "board-1", null, Instant.now()),
                )
            }

            val globalEvent = globalSink.asFlow().first()
            assertEquals("event", globalEvent.type)
        }
}
