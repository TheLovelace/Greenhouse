package org.greenhouse.ui.navigation

import androidx.navigation3.runtime.NavKey

sealed interface AppRoute : NavKey

data object HomeRoute : AppRoute

data object CategoriaRoute : AppRoute

data object AlertaRoute : AppRoute

data object EspecieRoute : AppRoute

data object ClienteRoute : AppRoute

data object CompraFornecedorRoute : AppRoute

data object DescontoListaRoute : AppRoute

data class DescontoFormularioRoute(
    val descontoId: Long?,
) : AppRoute

data object EstoqueListaRoute : AppRoute

data class EstoqueFormularioRoute(
    val produtoId: Long?,
) : AppRoute

data object FuncionarioRoute : AppRoute

data object FormaPagamentoRoute : AppRoute

data object FornecedorRoute : AppRoute

data object PedidoRoute : AppRoute

data object ProdutoRoute : AppRoute
