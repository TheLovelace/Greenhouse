@file:Suppress("ktlint:standard:function-naming")

package org.greenhouse

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import org.greenhouse.ui.components.ToastNotification
import org.greenhouse.ui.components.ToastNotificationData
import org.greenhouse.ui.components.ToastNotificationType
import org.greenhouse.ui.navigation.AlertaRoute
import org.greenhouse.ui.navigation.AppRoute
import org.greenhouse.ui.navigation.CategoriaRoute
import org.greenhouse.ui.navigation.ClienteRoute
import org.greenhouse.ui.navigation.CompraFornecedorRoute
import org.greenhouse.ui.navigation.DescontoFormularioRoute
import org.greenhouse.ui.navigation.DescontoListaRoute
import org.greenhouse.ui.navigation.EspecieRoute
import org.greenhouse.ui.navigation.EstoqueFormularioRoute
import org.greenhouse.ui.navigation.EstoqueListaRoute
import org.greenhouse.ui.navigation.FormaPagamentoRoute
import org.greenhouse.ui.navigation.FornecedorRoute
import org.greenhouse.ui.navigation.FuncionarioRoute
import org.greenhouse.ui.navigation.HomeRoute
import org.greenhouse.ui.navigation.PedidoRoute
import org.greenhouse.ui.navigation.ProdutoRoute
import org.greenhouse.ui.screens.alerta.CadastrarAlertaIotScreen
import org.greenhouse.ui.screens.categoria.CategoriaCadastro
import org.greenhouse.ui.screens.categoria.model.CategoriaCadastroData
import org.greenhouse.ui.screens.cliente.ClienteCadastro
import org.greenhouse.ui.screens.cliente.model.ClienteCadastroData
import org.greenhouse.ui.screens.compraFornecedor.CompraFornecedorCadastro
import org.greenhouse.ui.screens.compraFornecedor.model.CompraFornecedorCadastroData
import org.greenhouse.ui.screens.desconto.DescontoEdicao
import org.greenhouse.ui.screens.desconto.DescontoLista
import org.greenhouse.ui.screens.desconto.model.DescontoCadastroData
import org.greenhouse.ui.screens.desconto.model.DescontoItem
import org.greenhouse.ui.screens.especie.CadastrarEspecieScreen
import org.greenhouse.ui.screens.estoque.EstoqueEdicao
import org.greenhouse.ui.screens.estoque.EstoqueLista
import org.greenhouse.ui.screens.estoque.model.EstoqueCadastroData
import org.greenhouse.ui.screens.estoque.model.EstoqueItem
import org.greenhouse.ui.screens.formaPagamento.FormaPagamentoCadastro
import org.greenhouse.ui.screens.formaPagamento.model.FormaPagamentoCadastroData
import org.greenhouse.ui.screens.fornecedor.FornecedorCadastro
import org.greenhouse.ui.screens.fornecedor.model.FornecedorCadastroData
import org.greenhouse.ui.screens.funcionario.FuncionarioCadastro
import org.greenhouse.ui.screens.funcionario.model.FuncionarioCadastroData
import org.greenhouse.ui.screens.home.HomeDestination
import org.greenhouse.ui.screens.home.HomeScreen
import org.greenhouse.ui.screens.pedido.PedidoCadastro
import org.greenhouse.ui.screens.pedido.model.PedidoCadastroData
import org.greenhouse.ui.screens.produto.ProdutoCadastro
import org.greenhouse.ui.screens.produto.model.ProdutoCadastroData
import org.greenhouse.ui.theme.GreenhouseTheme

@Composable
fun App(
    onNavigate: (HomeDestination) -> Unit = {},
    onSaveCategoria: (CategoriaCadastroData) -> Unit = {},
    onSaveCliente: (ClienteCadastroData) -> Unit = {},
    onSaveCompraFornecedor: (CompraFornecedorCadastroData) -> Unit = {},
    onSaveDesconto: (DescontoCadastroData) -> Unit = {},
    onSaveEstoque: (EstoqueCadastroData) -> Unit = {},
    onSaveFuncionario: (FuncionarioCadastroData) -> Unit = {},
    onSaveFormaPagamento: (FormaPagamentoCadastroData) -> Unit = {},
    onSaveFornecedor: (FornecedorCadastroData) -> Unit = {},
    onSavePedido: (PedidoCadastroData) -> Unit = {},
    onSaveProduto: (ProdutoCadastroData) -> Unit = {},
) {
    val backStack = remember { mutableStateListOf<AppRoute>(HomeRoute) }
    var descontos by remember { mutableStateOf(emptyList<DescontoItem>()) }
    var estoques by remember { mutableStateOf(emptyList<EstoqueItem>()) }
    var toastNotification by remember { mutableStateOf<ToastNotificationData?>(null) }

    fun navigateBack() {
        if (backStack.size > 1) {
            backStack.removeAt(backStack.lastIndex)
        }
    }

    GreenhouseTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            NavDisplay(
                modifier = Modifier.fillMaxSize(),
                backStack = backStack,
                onBack = ::navigateBack,
                entryProvider =
                    entryProvider {
                        entry<HomeRoute> {
                            HomeScreen(
                                onNavigate = { destination ->
                                    backStack.add(
                                        when (destination) {
                                            HomeDestination.CATEGORIAS -> CategoriaRoute
                                            HomeDestination.CLIENTES -> ClienteRoute
                                            HomeDestination.COMPRAS -> CompraFornecedorRoute
                                            HomeDestination.DESCONTOS -> DescontoListaRoute
                                            HomeDestination.ESTOQUE -> EstoqueListaRoute
                                            HomeDestination.FUNCIONARIOS -> FuncionarioRoute
                                            HomeDestination.FORMAS_PAGAMENTO -> FormaPagamentoRoute
                                            HomeDestination.FORNECEDORES -> FornecedorRoute
                                            HomeDestination.PEDIDOS -> PedidoRoute
                                            HomeDestination.PRODUTOS -> ProdutoRoute
                                            HomeDestination.ESPECIES -> EspecieRoute
                                            HomeDestination.ALERTAS -> AlertaRoute
                                        },
                                    )
                                },
                            )
                        }
                        entry<CategoriaRoute> {
                            CategoriaCadastro(
                                onBack = ::navigateBack,
                                onSave = onSaveCategoria,
                            )
                        }
                        entry<ClienteRoute> {
                            ClienteCadastro(
                                onBack = ::navigateBack,
                                onSave = onSaveCliente,
                            )
                        }
                        entry<CompraFornecedorRoute> {
                            CompraFornecedorCadastro(
                                onBack = ::navigateBack,
                                onSave = onSaveCompraFornecedor,
                            )
                        }
                        entry<DescontoListaRoute> {
                            DescontoLista(
                                descontos = descontos,
                                onBack = ::navigateBack,
                                onCreate = { backStack.add(DescontoFormularioRoute(null)) },
                                onEdit = { desconto -> backStack.add(DescontoFormularioRoute(desconto.id)) },
                                onDelete = { desconto ->
                                    descontos = descontos.map { if (it.id == desconto.id) it.copy(ativo = false) else it }
                                    toastNotification =
                                        ToastNotificationData(
                                            message = "Desconto inativado com sucesso.",
                                            type = ToastNotificationType.DESTRUCTIVE,
                                        )
                                },
                            )
                        }
                        entry<DescontoFormularioRoute> { route ->
                            val desconto = descontos.firstOrNull { it.id == route.descontoId }
                            DescontoEdicao(
                                desconto = desconto,
                                onBack = ::navigateBack,
                                onSave = { data ->
                                    onSaveDesconto(data)
                                    val id = route.descontoId ?: ((descontos.maxOfOrNull { it.id } ?: 0L) + 1L)
                                    val item =
                                        DescontoItem(
                                            id = id,
                                            titulo = data.titulo,
                                            descricao = data.descricao,
                                            tipo = data.tipo,
                                            valor = data.valor,
                                            dataInicio = data.dataInicio,
                                            dataFim = data.dataFim,
                                            ativo = data.ativo,
                                            quantidadeMinimaItens = data.quantidadeMinimaItens.toInt(),
                                        )
                                    descontos =
                                        if (route.descontoId == null) {
                                            descontos + item
                                        } else {
                                            descontos.map { if (it.id == id) item else it }
                                        }
                                    toastNotification =
                                        ToastNotificationData(
                                            message =
                                                if (route.descontoId == null) {
                                                    "Desconto cadastrado com sucesso."
                                                } else {
                                                    "Desconto atualizado com sucesso."
                                                },
                                            type = ToastNotificationType.SUCCESS,
                                        )
                                    navigateBack()
                                },
                            )
                        }
                        entry<EstoqueListaRoute> {
                            EstoqueLista(
                                estoques = estoques,
                                onBack = ::navigateBack,
                                onCreate = { backStack.add(EstoqueFormularioRoute(null)) },
                                onEdit = { estoque -> backStack.add(EstoqueFormularioRoute(estoque.produtoId)) },
                                onDelete = { estoque ->
                                    estoques = estoques.filterNot { it.produtoId == estoque.produtoId }
                                    toastNotification =
                                        ToastNotificationData(
                                            message = "Estoque excluído com sucesso.",
                                            type = ToastNotificationType.DESTRUCTIVE,
                                        )
                                },
                            )
                        }
                        entry<EstoqueFormularioRoute> { route ->
                            val estoque = estoques.firstOrNull { it.produtoId == route.produtoId }
                            EstoqueEdicao(
                                estoque = estoque,
                                existingProdutoIds = estoques.map { it.produtoId }.toSet(),
                                onBack = ::navigateBack,
                                onSave = { data ->
                                    onSaveEstoque(data)
                                    val produtoId = data.produtoId
                                    val quantidadeReservada = estoque?.quantidadeReservada ?: 0
                                    val item =
                                        EstoqueItem(
                                            produtoId = produtoId,
                                            quantidadeFisica = data.quantidadeFisica,
                                            quantidadeReservada = quantidadeReservada,
                                            quantidadeDisponivel = data.quantidadeFisica - quantidadeReservada,
                                            quantidadeMinima = data.quantidadeMinima,
                                            localizacao = data.localizacao,
                                            bloqueado = data.bloqueado,
                                        )
                                    estoques =
                                        if (route.produtoId == null) {
                                            estoques + item
                                        } else {
                                            estoques.map { if (it.produtoId == route.produtoId) item else it }
                                        }
                                    toastNotification =
                                        ToastNotificationData(
                                            message =
                                                if (route.produtoId == null) {
                                                    "Estoque cadastrado com sucesso."
                                                } else {
                                                    "Estoque atualizado com sucesso."
                                                },
                                            type = ToastNotificationType.SUCCESS,
                                        )
                                    navigateBack()
                                },
                            )
                        }
                        entry<FuncionarioRoute> {
                            FuncionarioCadastro(
                                onBack = ::navigateBack,
                                onSave = onSaveFuncionario,
                            )
                        }
                        entry<FormaPagamentoRoute> {
                            FormaPagamentoCadastro(
                                onBack = ::navigateBack,
                                onSave = onSaveFormaPagamento,
                            )
                        }
                        entry<FornecedorRoute> {
                            FornecedorCadastro(
                                onBack = ::navigateBack,
                                onSave = onSaveFornecedor,
                            )
                        }
                        entry<PedidoRoute> {
                            PedidoCadastro(
                                onBack = ::navigateBack,
                                onSave = onSavePedido,
                            )
                        }
                        entry<ProdutoRoute> {
                            ProdutoCadastro(
                                onBack = ::navigateBack,
                                onSave = onSaveProduto,
                            )
                        }
                        entry<AlertaRoute> {
                            CadastrarAlertaIotScreen(
                                onBackClick = ::navigateBack,
                                onSalvarClick = { _, _, _, _, _, _, _, _ -> navigateBack() },
                            )
                        }
                        entry<EspecieRoute> {
                            CadastrarEspecieScreen(
                                onBackClick = ::navigateBack,
                                onSalvarClick = { _ -> navigateBack() },
                            )
                        }
                    },
            )
            ToastNotification(
                notification = toastNotification,
                onDismiss = { toastNotification = null },
                modifier = Modifier.align(Alignment.TopEnd).padding(top = 16.dp, end = 16.dp, start = 16.dp),
            )
        }
    }
}
