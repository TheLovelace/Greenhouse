package org.greenhouse.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "fornecedor")
data class Fornecedor(
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @field:Column(nullable = false, length = 150)
    var nome: String = "",
    @field:Column(length = 14, unique = true)
    var cnpj: String? = null,
    @field:Column(length = 20)
    var telefone: String? = null,
    @field:Column(length = 150)
    var email: String? = null,
    @field:Column(length = 150)
    var contato: String? = null,
    @field:Column(nullable = false)
    var ativo: Boolean = true,
    @field:Column(name = "prazo_medio_entrega_dias")
    var prazoMedioEntregaDias: Int? = null,
)
