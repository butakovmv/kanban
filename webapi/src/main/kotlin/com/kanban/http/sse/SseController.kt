package com.kanban.http.sse

import com.kanban.sse.SinkService
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
        return sink
            .asFlow()
            .map { event ->
                ServerSentEvent
                    .builder<String>()
                    .id(event.timestamp.toEpochMilli().toString())
                    .event(event.type)
                    .data(event.data)
                    .build()
            }.asFlux()
            .doOnCancel { sinkService.unregister(sink) }
            .doOnTerminate { sinkService.unregister(sink) }
    }
}
