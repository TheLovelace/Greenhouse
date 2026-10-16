package org.greenhouse.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.greenhouse.enums.StatusCompra
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime

@Entity
@Table(name = "compra_fornecedor")
data class CompraFornecedor(
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @field:Column(name = "funcionario_id", nullable = false)
    var funcionarioId: Long = 0,
    @field:Column(name = "fornecedor_id", nullable = false)
    var fornecedorId: Long = 0,
    @field:Column(name = "forma_pagamento_id", nullable = false)
    var formaPagamentoId: Long = 0,
    @field:Column(name = "data_compra", nullable = false)
    var dataCompra: OffsetDateTime = OffsetDateTime.now(),
    @field:Enumerated(EnumType.STRING)
    @field:Column(nullable = false, length = 20)
    var status: StatusCompra = StatusCompra.PEDIDA,
    @field:Column(name = "valor_total", nullable = false, precision = 12, scale = 2)
    var valorTotal: BigDecimal = BigDecimal.ZERO,
    @field:Column(name = "valor_frete", precision = 12, scale = 2)
    var valorFrete: BigDecimal? = null,
    @field:Column(name = "frete_incluso", nullable = false)
    var freteIncluso: Boolean = false,
    @field:Column(name = "data_entrega_prevista")
    var dataEntregaPrevista: LocalDate? = null,
    @field:Column(length = 1000)
    var observacao: String? = null,
)
