package org.greenhouse.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "usuario")
data class Usuario(
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @field:Column(name = "funcionario_id", nullable = false, unique = true)
    var funcionarioId: Long = 0,
    @field:Column(nullable = false, length = 100, unique = true)
    var login: String = "",
    @field:Column(name = "senha_hash", nullable = false, length = 255)
    var senhaHash: String = "",
    @field:Column(nullable = false, length = 20)
    var perfil: String = "",
    @field:Column(nullable = false)
    var ativo: Boolean = true,
    @field:Column(name = "primeiro_acesso_feito", nullable = false)
    var primeiroAcessoFeito: Boolean = false,
)
