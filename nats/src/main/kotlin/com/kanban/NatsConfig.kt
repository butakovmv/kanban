package com.kanban

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnProperty(name = ["kanban.nats.enabled"], havingValue = "false", matchIfMissing = true)
class NatsConfig {
}
