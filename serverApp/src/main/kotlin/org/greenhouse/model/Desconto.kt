package org.greenhouse.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.greenhouse.enums.TipoDesconto
import java.math.BigDecimal
import java.time.OffsetDateTime

@Entity
@Table(name = "desconto")
data class Desconto(
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @field:Column(nullable = false, length = 150)
    var titulo: String = "",
    @field:Column(length = 1000)
    var descricao: String? = null,
    @field:Enumerated(EnumType.STRING)
    @field:Column(nullable = false, length = 20)
    var tipo: TipoDesconto = TipoDesconto.PERCENTUAL,
    @field:Column(nullable = false, precision = 12, scale = 2)
    var valor: BigDecimal = BigDecimal.ZERO,
    @field:Column(name = "data_inicio")
    var dataInicio: OffsetDateTime? = null,
    @field:Column(name = "data_fim")
    var dataFim: OffsetDateTime? = null,
    @field:Column(nullable = false)
    var ativo: Boolean = true,
    @field:Column(name = "quantidade_minima_itens", nullable = false)
    var quantidadeMinimaItens: Int = 1,
)
