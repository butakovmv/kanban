package com.kanban

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration

/**
 * Конфигурация NATS-адаптера.
 * По умолчанию отключена (kanban.nats.enabled=false).
 * При включении подключает распределённый брокер сообщений
 * для межсервисного взаимодействия.
 */
@Configuration
@ConditionalOnProperty(name = ["kanban.nats.enabled"], havingValue = "false", matchIfMissing = true)
class NatsConfig
