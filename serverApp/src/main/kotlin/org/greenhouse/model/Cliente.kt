package org.greenhouse.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "cliente")
data class Cliente(
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @field:Column(nullable = false, length = 150)
    var nome: String = "",
    @field:Column(nullable = false, length = 20)
    var telefone: String = "",
    @field:Column(nullable = false)
    var whatsapp: Boolean = false,
    @field:Column(length = 11, unique = true)
    var cpf: String? = null,
    @field:Column(length = 150)
    var email: String? = null,
    @field:Column(nullable = false)
    var ativo: Boolean = true,
)
