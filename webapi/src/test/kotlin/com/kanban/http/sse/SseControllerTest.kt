package com.kanban.http.sse

import com.kanban.sse.SinkService
import com.kanban.sse.SseEvent
import java.time.Instant
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import reactor.test.StepVerifier

internal class SseControllerTest {
    @Test
    fun `should stream events via controller`() {
        val sinkService = SinkService()
        val controller = SseController(sinkService)

        val flux = controller.stream(null, null)

        StepVerifier
            .create(flux)
            .then {
                runBlocking {
                    sinkService.emit(
                        SseEvent("test_event", """{"key":"value"}""", null, null, Instant.now()),
                    )
                }
            }.expectNextMatches { event -> event.event() == "test_event" }
            .thenCancel()
            .verify()
    }
}
