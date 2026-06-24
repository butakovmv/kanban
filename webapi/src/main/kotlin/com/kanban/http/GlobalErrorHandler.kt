package com.kanban.http

import java.net.URI
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.dao.DuplicateKeyException
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.server.MissingRequestValueException
import org.springframework.web.server.ServerWebInputException

/**
 * Глобальный обработчик ошибок для REST-контроллеров.
 * Перехватывает исключения и возвращает ответ в формате Problem Detail (RFC 7807).
 */
@RestControllerAdvice
class GlobalErrorHandler {
    private fun getRequestId(): String? = MDC.get("requestId")

    /**
     * Обрабатывает ошибки валидации запросов (WebExchangeBindException).
     * Формирует ProblemDetail со статусом 400 BAD_REQUEST и списком ошибок полей.
     */
    @ExceptionHandler(WebExchangeBindException::class)
    fun handleValidation(ex: WebExchangeBindException): ProblemDetail {
        log.warn("Validation error: ${ex.fieldErrors.joinToString("; ") { "${it.field}: ${it.defaultMessage}" }}")
        val detail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST)
        detail.title = "Validation Error"
        detail.detail = ex.fieldErrors.joinToString("; ") { "${it.field}: ${it.defaultMessage}" }
        detail.type = URI.create("/errors/validation")
        detail.setProperty("requestId", getRequestId())
        return detail
    }

    @ExceptionHandler(MissingRequestValueException::class)
    fun handleMissingRequestValue(ex: MissingRequestValueException): ProblemDetail {
        log.warn("Missing request value: ${ex.reason}")
        val detail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST)
        detail.title = "Bad Request"
        detail.detail = ex.reason ?: "Missing required request value"
        detail.type = URI.create("/errors/bad-request")
        detail.setProperty("requestId", getRequestId())
        return detail
    }

    @ExceptionHandler(ServerWebInputException::class)
    fun handleServerWebInput(ex: ServerWebInputException): ProblemDetail {
        log.warn("Server web input error: ${ex.reason}")
        val detail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST)
        detail.title = "Bad Request"
        detail.detail = ex.reason ?: "Invalid input"
        detail.type = URI.create("/errors/bad-request")
        detail.setProperty("requestId", getRequestId())
        return detail
    }

    @ExceptionHandler(DuplicateKeyException::class)
    fun handleDuplicateKey(ex: DuplicateKeyException): ProblemDetail {
        log.warn("Duplicate key: ${ex.message}")
        val detail = ProblemDetail.forStatus(HttpStatus.CONFLICT)
        detail.title = "Conflict"
        detail.detail = ex.message ?: "Resource already exists"
        detail.type = URI.create("/errors/conflict")
        detail.setProperty("requestId", getRequestId())
        return detail
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ProblemDetail {
        log.warn("Invalid request argument: ${ex.message}")
        val detail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST)
        detail.title = "Bad Request"
        detail.detail = ex.message ?: "Invalid argument"
        detail.type = URI.create("/errors/bad-request")
        detail.setProperty("requestId", getRequestId())
        return detail
    }

    /**
     * Обрабатывает все необработанные исключения (Throwable).
     * Формирует ProblemDetail со статусом 500 INTERNAL_SERVER_ERROR.
     */
    @ExceptionHandler(Throwable::class)
    fun handleGeneral(ex: Throwable): ProblemDetail {
        log.error("Unhandled exception", ex)
        val detail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR)
        detail.title = "Internal Error"
        detail.detail = ex.message ?: "Unknown error"
        detail.type = URI.create("/errors/internal")
        detail.setProperty("requestId", getRequestId())
        return detail
    }

    companion object {
        private val log = LoggerFactory.getLogger(GlobalErrorHandler::class.java)
    }
}
