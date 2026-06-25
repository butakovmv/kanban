package com.kanban.sse

import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.springframework.stereotype.Service

@Service
internal class SinkService {
    private val globalSinks = ConcurrentHashMap.newKeySet<Sink>()
    private val boardSinks = ConcurrentHashMap<String, MutableSet<Sink>>()
    private val projectSinks = ConcurrentHashMap<String, MutableSet<Sink>>()

    fun register(
        boardId: String? = null,
        projectId: String? = null,
    ): Sink {
        val sink = Sink()
        if (boardId != null) {
            boardSinks.computeIfAbsent(boardId) { ConcurrentHashMap.newKeySet() }.add(sink)
        } else if (projectId != null) {
            projectSinks.computeIfAbsent(projectId) { ConcurrentHashMap.newKeySet() }.add(sink)
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
