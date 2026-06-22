package com.kanban.http

import java.util.UUID
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.core.annotation.Order
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Component
@Order(0)
internal class LoggingWebFilter : WebFilter {
    private val log = LoggerFactory.getLogger(LoggingWebFilter::class.java)

    override fun filter(
        exchange: ServerWebExchange,
        chain: WebFilterChain,
    ): Mono<Void> {
        val request = exchange.request
        val requestId = UUID.randomUUID().toString().take(8)
        val method = request.method
        val path = request.uri.path
        val query = request.uri.query

        MDC.put("requestId", requestId)
        MDC.put("request", "$method $path")

        val fullPath = if (query != null) "$path?$query" else path
        log.info(">>> {} {}", method, fullPath)

        val start = System.currentTimeMillis()
        return chain.filter(exchange).doFinally {
            val elapsed = System.currentTimeMillis() - start
            val status = extractStatus(exchange.response)
            log.info("<<< {} {} {} {}ms", status, method, fullPath, elapsed)
            MDC.remove("requestId")
            MDC.remove("request")
        }
    }

    private fun extractStatus(response: ServerHttpResponse): Int = response.statusCode?.value() ?: 0
}
