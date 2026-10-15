package org.greenhouse.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate

@Entity
@Table(name = "funcionario")
data class Funcionario(
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @field:Column(nullable = false, length = 150)
    var nome: String = "",
    @field:Column(nullable = false, length = 11, unique = true)
    var cpf: String = "",
    @field:Column(length = 20)
    var telefone: String? = null,
    @field:Column(length = 150, unique = true)
    var email: String? = null,
    @field:Column(length = 100)
    var cargo: String? = null,
    @field:Column(nullable = false)
    var ativo: Boolean = true,
    @field:Column(name = "data_de_nascimento")
    var dataDeNascimento: LocalDate? = null,
)
