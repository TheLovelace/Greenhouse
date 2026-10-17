package org.greenhouse.adapter

import org.springframework.http.HttpStatus

sealed class AppException(
    message: String,
    val statusCode: HttpStatus,
    cause: Throwable? = null,
) : Exception(message, cause)

/** 503 - Falha ao conectar ou se comunicar com o banco de dados. */
class DatabaseConnectionException(cause: Throwable? = null) :
    AppException(
        message = "Falha ao conectar ao banco de dados",
        statusCode = HttpStatus.SERVICE_UNAVAILABLE,
        cause = cause,
    )

/** 401 - Credenciais inválidas ou ausentes. */
class AuthenticationException(message: String = "Falha de autenticação", cause: Throwable? = null) :
    AppException(
        message = message,
        statusCode = HttpStatus.UNAUTHORIZED,
        cause = cause,
    )

/** 400 - Requisição malformada (sintaxe, campos ausentes, tipos errados). */
class BadRequestException(message: String = "Requisição inválida", cause: Throwable? = null) :
    AppException(
        message = message,
        statusCode = HttpStatus.BAD_REQUEST,
        cause = cause,
    )

/** 400 - Requisição bem formada, porém com dados que não podem ser processados. */
class UnprocessableEntityException(message: String = "Entidade não processável", cause: Throwable? = null) :
    AppException(
        message = message,
        statusCode = HttpStatus.BAD_REQUEST,
        cause = cause,
    )

/** 404 - Recurso não encontrado. */
class NotFoundException(message: String = "Recurso não encontrado", cause: Throwable? = null) :
    AppException(
        message = message,
        statusCode = HttpStatus.NOT_FOUND,
        cause = cause,
    )

/** 409 - O recurso já existe e não pode ser criado novamente. */
class ConflictException(message: String = "Conflito com o estado atual do recurso", cause: Throwable? = null) :
    AppException(
        message = message,
        statusCode = HttpStatus.CONFLICT,
        cause = cause,
    )

/** 500 - Erro interno inesperado. */
class ServerException(message: String = "Erro interno do servidor", cause: Throwable? = null) :
    AppException(
        message = message,
        statusCode = HttpStatus.INTERNAL_SERVER_ERROR,
        cause = cause,
    )