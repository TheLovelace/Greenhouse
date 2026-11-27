@file:Suppress("ktlint:standard:class-naming")

package org.greenhouse.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface ModulosRotas : NavKey {
    @Serializable
    object categorias : ModulosRotas

    @Serializable
    object produtos : ModulosRotas

    @Serializable
    object descontos : ModulosRotas

    @Serializable
    object estoque : ModulosRotas

    @Serializable
    object funcionarios : ModulosRotas

    @Serializable
    object fornecedores : ModulosRotas

    @Serializable
    object formasPagamento : ModulosRotas

    @Serializable
    object compras : ModulosRotas
}

sealed interface CategoriaRotas : NavKey {
    @Serializable
    object formulario : CategoriaRotas

    @Serializable
    object listagem : CategoriaRotas
}

sealed interface ProdutoRotas : NavKey {
    @Serializable
    object formulario : ProdutoRotas

    @Serializable
    object listagem : ProdutoRotas
}

sealed interface DescontoRotas : NavKey {
    @Serializable
    data class formulario(
        val descontoId: Long?,
    ) : DescontoRotas

    @Serializable
    object listagem : DescontoRotas
}

sealed interface EstoqueRotas : NavKey {
    @Serializable
    data class formulario(
        val produtoId: Long?,
    ) : EstoqueRotas

    @Serializable
    object listagem : EstoqueRotas
}
