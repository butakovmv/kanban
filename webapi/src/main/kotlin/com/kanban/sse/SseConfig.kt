package com.kanban.sse

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(SinkService::class)
internal class SseConfig
