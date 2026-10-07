package org.greenhouse.dto

data class FornecedorDTO(
    val id: Long? = null,
    val nome: String,
    val cnpj: String? = null,
    val telefone: String? = null,
    val email: String? = null,
    val contato: String? = null,
    val ativo: Boolean = true,
)
