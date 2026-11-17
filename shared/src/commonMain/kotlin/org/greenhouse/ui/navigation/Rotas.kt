@file:Suppress("ktlint:standard:class-naming")

package org.greenhouse.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface ModulosRotas : NavKey {
    @Serializable
    object categorias : ModulosRotas

    @Serializable
    object produtos : ModulosRotas
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
