package org.greenhouse.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "forma_pagamento")
data class FormaPagamento(
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @field:Column(nullable = false, length = 50, unique = true)
    var nome: String = "",
    @field:Column(nullable = false)
    var ativo: Boolean = true,
)
