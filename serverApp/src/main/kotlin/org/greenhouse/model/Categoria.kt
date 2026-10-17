package org.greenhouse.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.DynamicInsert
import java.time.LocalDate

@Entity
@DynamicInsert
@Table(name = "categoria")
data class Categoria(
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @field:Column(nullable = false, length = 100, unique = true)
    var nome: String = "",
    @field:Column(nullable = false, length = 1000)
    var descricao: String = "",
    @field:Column(nullable = false)
    var ativo: Boolean = true,
    @field:Column(length = 300)
    var observacao: String? = null,
    @field:Column(name = "data_cadastro", nullable = false)
    var dataCadastro: LocalDate? = null,
)
