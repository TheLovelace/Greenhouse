package org.greenhouse.dto

import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Digits
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import java.math.BigDecimal

data class EspecieRequestDTO(
    @field:NotBlank(message = "O nome popular é obrigatório")
    @field:Size(max = 150, message = "O nome popular deve ter no máximo 150 caracteres")
    val nomePopular: String,

    @field:NotBlank(message = "O nome científico é obrigatório")
    @field:Size(max = 150, message = "O nome científico deve ter no máximo 150 caracteres")
    val nomeCientifico: String,

    @field:Size(max = 2000, message = "A descrição deve ter no máximo 2000 caracteres")
    val descricao: String? = null,

    @field:NotNull(message = "O intervalo de rega é obrigatório")
    @field:Positive(message = "O intervalo de rega deve ser positivo")
    val intervaloRegaDias: Int,

    @field:NotNull(message = "A temperatura mínima é obrigatória")
    @field:Digits(integer = 3, fraction = 2, message = "A temperatura mínima deve ter até 3 dígitos inteiros e 2 decimais")
    val temperaturaMin: BigDecimal,

    @field:NotNull(message = "A temperatura máxima é obrigatória")
    @field:Digits(integer = 3, fraction = 2, message = "A temperatura máxima deve ter até 3 dígitos inteiros e 2 decimais")
    val temperaturaMax: BigDecimal,

    @field:DecimalMin("0.0", message = "A umidade do ar mínima deve estar entre 0 e 100")
    @field:DecimalMax("100.0", message = "A umidade do ar mínima deve estar entre 0 e 100")
    val umidadeArMin: BigDecimal? = null,

    @field:DecimalMin("0.0", message = "A umidade do ar máxima deve estar entre 0 e 100")
    @field:DecimalMax("100.0", message = "A umidade do ar máxima deve estar entre 0 e 100")
    val umidadeArMax: BigDecimal? = null,

    @field:DecimalMin("0.0", message = "A umidade do solo mínima deve estar entre 0 e 100")
    @field:DecimalMax("100.0", message = "A umidade do solo mínima deve estar entre 0 e 100")
    val umidadeSoloMin: BigDecimal? = null,

    @field:DecimalMin("0.0", message = "A umidade do solo máxima deve estar entre 0 e 100")
    @field:DecimalMax("100.0", message = "A umidade do solo máxima deve estar entre 0 e 100")
    val umidadeSoloMax: BigDecimal? = null,
)