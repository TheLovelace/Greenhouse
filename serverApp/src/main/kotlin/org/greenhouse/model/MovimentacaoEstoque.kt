package org.greenhouse.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.greenhouse.enums.TipoMovimentacaoEstoque
import org.hibernate.annotations.Generated
import org.hibernate.generator.EventType
import java.time.OffsetDateTime

@Entity
@Table(name = "movimentacao_estoque")
data class MovimentacaoEstoque(
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @field:Column(name = "produto_id", nullable = false)
    var produtoId: Long = 0,
    @field:Column(name = "pedido_id")
    var pedidoId: Long? = null,
    @field:Column(name = "compra_id")
    var compraId: Long? = null,
    @field:Column(name = "funcionario_id")
    var funcionarioId: Long? = null,
    @field:Enumerated(EnumType.STRING)
    @field:Column(nullable = false, length = 30)
    var tipo: TipoMovimentacaoEstoque = TipoMovimentacaoEstoque.ENTRADA,
    @field:Column(nullable = false)
    var quantidade: Int = 0,
    @field:Column(columnDefinition = "TEXT")
    var observacao: String? = null,
    @field:Generated(event = [EventType.INSERT])
    @field:Column(name = "data_hora", nullable = false, insertable = false, updatable = false)
    var dataHora: OffsetDateTime? = null,
)
