package org.greenhouse.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "estoque")
data class Estoque(
    @field:Id
    @field:Column(name = "produto_id")
    var produtoId: Long = 0,
    @field:Column(name = "quantidade_fisica", nullable = false)
    var quantidadeFisica: Int = 0,
    @field:Column(name = "quantidade_reservada", nullable = false)
    var quantidadeReservada: Int = 0,
    @field:Column(name = "quantidade_minima", nullable = false)
    var quantidadeMinima: Int = 0,
)
