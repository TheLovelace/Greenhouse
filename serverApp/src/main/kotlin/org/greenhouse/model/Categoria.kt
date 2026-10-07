package org.greenhouse.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "categoria")
data class Categoria(
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @field:Column(nullable = false, length = 100, unique = true)
    var nome: String = "",
    @field:Column(nullable = false, columnDefinition = "TEXT")
    var descricao: String = "",
    @field:Column(nullable = false)
    var ativo: Boolean = true,
)
