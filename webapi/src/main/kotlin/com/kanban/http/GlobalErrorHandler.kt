package com.kanban.http

import java.net.URI
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException

/**
 * Глобальный обработчик ошибок для REST-контроллеров.
 * Перехватывает исключения и возвращает ответ в формате Problem Detail (RFC 7807).
 */
@RestControllerAdvice
class GlobalErrorHandler {
    /**
     * Обрабатывает ошибки валидации запросов (WebExchangeBindException).
     * Формирует ProblemDetail со статусом 400 BAD_REQUEST и списком ошибок полей.
     *
     * @param ex исключение валидации с информацией об ошибках полей
     * @return ProblemDetail с деталями ошибки валидации
     */
    @ExceptionHandler(WebExchangeBindException::class)
    fun handleValidation(ex: WebExchangeBindException): ProblemDetail {
        val detail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST)
        detail.title = "Validation Error"
        detail.detail = ex.fieldErrors.joinToString("; ") { "${it.field}: ${it.defaultMessage}" }
        detail.type = URI.create("/errors/validation")
        return detail
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ProblemDetail {
        log.warn("Invalid request argument", ex)
        val detail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST)
        detail.title = "Bad Request"
        detail.detail = ex.message ?: "Invalid argument"
        detail.type = URI.create("/errors/bad-request")
        return detail
    }

    /**
     * Обрабатывает все необработанные исключения (Throwable).
     * Формирует ProblemDetail со статусом 500 INTERNAL_SERVER_ERROR.
     *
     * @param ex непредвиденное исключение
     * @return ProblemDetail с информацией об ошибке
     */
    @ExceptionHandler(Throwable::class)
    fun handleGeneral(ex: Throwable): ProblemDetail {
        log.error("Unhandled exception", ex)
        val detail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR)
        detail.title = "Internal Error"
        detail.detail = ex.message ?: "Unknown error"
        detail.type = URI.create("/errors/internal")
        return detail
    }

    companion object {
        private val log = LoggerFactory.getLogger(GlobalErrorHandler::class.java)
    }
}
