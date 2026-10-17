package org.greenhouse.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.greenhouse.enums.ModalidadePedido
import org.greenhouse.enums.StatusPedido
import java.math.BigDecimal
import java.time.OffsetDateTime

@Entity
@Table(name = "pedido")
data class Pedido(
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @field:Column(name = "cliente_id")
    var clienteId: Long? = null,
    @field:Column(name = "funcionario_id", nullable = false)
    var funcionarioId: Long = 0,
    @field:Column(name = "desconto_id")
    var descontoId: Long? = null,
    @field:Column(name = "forma_pagamento_id", nullable = false)
    var formaPagamentoId: Long = 0,
    @field:Enumerated(EnumType.STRING)
    @field:Column(nullable = false, length = 30)
    var modalidade: ModalidadePedido = ModalidadePedido.IMEDIATA,
    @field:Enumerated(EnumType.STRING)
    @field:Column(nullable = false, length = 30)
    var status: StatusPedido = StatusPedido.ABERTO,
    @field:Column(name = "data_pedido", nullable = false)
    var dataPedido: OffsetDateTime = OffsetDateTime.now(),
    @field:Column(name = "data_retirada_prevista")
    var dataRetiradaPrevista: OffsetDateTime? = null,
    @field:Column(name = "data_retirada")
    var dataRetirada: OffsetDateTime? = null,
    @field:Column(name = "valor_desconto", nullable = false, precision = 12, scale = 2)
    var valorDesconto: BigDecimal = BigDecimal.ZERO,
    @field:Column(name = "valor_total", nullable = false, precision = 12, scale = 2)
    var valorTotal: BigDecimal = BigDecimal.ZERO,
    @field:Column(length = 1000)
    var observacao: String? = null,
    @field:Column(name = "embalagem_presente", nullable = false)
    var embalagemPresente: Boolean = false,
    @field:Column(name = "numero_volumes", nullable = false)
    var numeroVolumes: Int = 1,
)
