package org.greenhouse.adapter

import jakarta.persistence.PersistenceException
import org.slf4j.LoggerFactory
import org.springframework.dao.DataAccessException
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.NoHandlerFoundException
import java.sql.SQLException

@RestControllerAdvice
class GlobalExceptionHandler {
    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(e: MethodArgumentNotValidException): ResponseEntity<Map<String, String>> {
        val errors = e.bindingResult.fieldErrors
            .joinToString("; ") { "${it.field}: ${it.defaultMessage}" }
        log.warn("Validação falhou: {}", errors)
        return ResponseEntity
            .badRequest()
            .body(mapOf("error" to errors))
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleInvalidRequestBody(e: HttpMessageNotReadableException): ResponseEntity<Map<String, String>> {
        log.warn("Corpo da requisição inválido: {}", (e.mostSpecificCause)::class.java.simpleName)
        return ResponseEntity
            .badRequest()
            .body(mapOf("error" to "Corpo da requisição inválido ou campos obrigatórios ausentes"))
    }

    /** Intercepta rotas inexistentes (nenhum @RequestMapping correspondente). */
    @ExceptionHandler(NoHandlerFoundException::class)
    fun handleNoHandlerFound(e: NoHandlerFoundException): ResponseEntity<Map<String, String?>> {
        val notFound = NotFoundException("Rota não encontrada: ${e.httpMethod} ${e.requestURL}")
        log.warn(notFound.message)
        return ResponseEntity
            .status(notFound.statusCode)
            .body(mapOf("error" to notFound.message))
    }

    /** Intercepta qualquer exceção de domínio (DatabaseConnectionException, AuthenticationException, etc). */
    @ExceptionHandler(AppException::class)
    fun handleAppException(e: AppException): ResponseEntity<Map<String, String?>> {
        logAppException(e)
        return ResponseEntity
            .status(e.statusCode)
            .body(mapOf("error" to e.message))
    }

    @ExceptionHandler(DataAccessException::class)
    fun handleDatabaseException(e: DataAccessException): ResponseEntity<Map<String, String?>> =
        handleAppException(DatabaseConnectionException(cause = e))

    @ExceptionHandler(PersistenceException::class)
    fun handlePersistenceException(e: PersistenceException): ResponseEntity<Map<String, String?>> =
        handleAppException(DatabaseConnectionException(cause = e))

    private fun logAppException(e: AppException) {
        val name = e::class.java.simpleName
        when {
            e is DatabaseConnectionException -> {
                val rootCause = rootCause(e)
                val sqlState =
                    generateSequence(e as Throwable) { it.cause }
                        .filterIsInstance<SQLException>()
                        .firstNotNullOfOrNull { it.sqlState }
                log.error(
                    "{}: {} | causa raiz: {} | SQLState: {}",
                    name,
                    e.message,
                    rootCause::class.java.name,
                    sqlState ?: "n/a",
                )
                log.debug("Detalhes da falha no banco de dados", e)
            }
            e.statusCode.is5xxServerError -> log.error("{}: {}", name, e.message, e)
            else -> log.warn("{} ({}): {}", name, e.statusCode.value(), e.message)
        }
    }

    private fun rootCause(e: Throwable): Throwable {
        var current = e
        while (current.cause != null && current.cause !== current) current = current.cause!!
        return current
    }
}
