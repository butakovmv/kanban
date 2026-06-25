package com.kanban.http.sse

import com.kanban.sse.SinkService
import com.kanban.sse.SseEvent
import java.time.Duration
import java.time.Instant
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactor.asFlux
import org.springframework.http.MediaType
import org.springframework.http.codec.ServerSentEvent
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
@RequestMapping("/api/v1/events")
internal class SseController(
    private val sinkService: SinkService,
) {
    @GetMapping(produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun stream(
        @RequestParam("board_id") boardId: String? = null,
        @RequestParam("project_id") projectId: String? = null,
    ): Flux<ServerSentEvent<String>> {
        val sink = sinkService.register(boardId, projectId)
        val heartbeat =
            Flux
                .interval(Duration.ofSeconds(15))
                .map { _: Long ->
                    ServerSentEvent
                        .builder<String>()
                        .id(Instant.now().toEpochMilli().toString())
                        .event("heartbeat")
                        .data("")
                        .build()
                }
        val events: Flux<ServerSentEvent<String>> =
            sink
                .asFlow()
                .map { event: SseEvent ->
                    ServerSentEvent
                        .builder<String>()
                        .id(event.timestamp.toEpochMilli().toString())
                        .event(event.type)
                        .data(event.data)
                        .build()
                }.asFlux()
        return Flux
            .merge(events, heartbeat)
            .doOnCancel { sinkService.unregister(sink) }
            .doOnTerminate { sinkService.unregister(sink) }
    }
}
