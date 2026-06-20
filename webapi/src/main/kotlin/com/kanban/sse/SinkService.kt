package com.kanban.sse

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.springframework.stereotype.Service

@Service
internal class SinkService {
    private val globalSinks = mutableListOf<Sink>()
    private val boardSinks = mutableMapOf<String, MutableList<Sink>>()
    private val projectSinks = mutableMapOf<String, MutableList<Sink>>()

    fun register(
        boardId: String? = null,
        projectId: String? = null,
    ): Sink {
        val sink = Sink()
        if (boardId != null) {
            boardSinks.getOrPut(boardId) { mutableListOf() }.add(sink)
        } else if (projectId != null) {
            projectSinks.getOrPut(projectId) { mutableListOf() }.add(sink)
        } else {
            globalSinks.add(sink)
        }
        return sink
    }

    fun unregister(sink: Sink) {
        globalSinks.remove(sink)
        boardSinks.values.forEach { it.remove(sink) }
        projectSinks.values.forEach { it.remove(sink) }
    }

    suspend fun emit(event: SseEvent) {
        val targets = mutableSetOf<Sink>()
        targets.addAll(globalSinks)
        event.boardId?.let { boardSinks[it]?.let { targets.addAll(it) } }
        event.projectId?.let { projectSinks[it]?.let { targets.addAll(it) } }
        targets.forEach { it.send(event) }
    }
}

internal class Sink {
    private val flow = MutableSharedFlow<SseEvent>(extraBufferCapacity = 64)

    suspend fun send(event: SseEvent) {
        flow.emit(event)
    }

    fun asFlow(): Flow<SseEvent> = flow.asSharedFlow()
}
