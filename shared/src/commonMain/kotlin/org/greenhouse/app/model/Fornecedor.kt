package org.greenhouse.app.model

import kotlinx.serialization.Serializable

@Serializable
data class Fornecedor(
    val id: Long,
    val nome: String,
    val cnpj: String?,
    val telefone: String?,
    val email: String?,
    val contato: String?,
    val ativo: Boolean,
    val prazoMedioEntregaDias: Int?,
)

@Serializable
data class FornecedorRequest(
    val nome: String,
    val cnpj: String?,
    val telefone: String?,
    val email: String?,
    val contato: String?,
    val ativo: Boolean,
    val prazoMedioEntregaDias: Int?,
)
