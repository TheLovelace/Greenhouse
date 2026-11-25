@file:Suppress("ktlint:standard:function-naming")

package org.greenhouse

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.greenhouse.app.repository.CategoriaRepositorioRemoto
import org.greenhouse.app.repository.ProdutoRepositorioRemoto
import org.greenhouse.app.repository.createHttpClient
import org.greenhouse.app.viewmodel.CategoriaViewModel
import org.greenhouse.app.viewmodel.PrincipalViewModel
import org.greenhouse.app.viewmodel.ProdutoViewModel
import org.greenhouse.ui.components.ToastNotification
import org.greenhouse.ui.components.ToastNotificationData
import org.greenhouse.ui.components.ToastNotificationType
import org.greenhouse.ui.navigation.ModulosRotas
import org.greenhouse.ui.screens.alerta.CadastrarAlertaIotScreen
import org.greenhouse.ui.screens.catalogo.CatalogoScreen
import org.greenhouse.ui.screens.cliente.ClienteCadastro
import org.greenhouse.ui.screens.cliente.model.ClienteCadastroData
import org.greenhouse.ui.screens.compraFornecedor.CompraFornecedorCadastro
import org.greenhouse.ui.screens.compraFornecedor.model.CompraFornecedorCadastroData
import org.greenhouse.ui.screens.desconto.model.DescontoCadastroData
import org.greenhouse.ui.screens.desconto.model.DescontoItem
import org.greenhouse.ui.screens.especie.CadastrarEspecieScreen
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
import org.greenhouse.ui.theme.GreenhouseTheme

@Composable
fun App(
    onNavigate: (HomeDestination) -> Unit = {},
    onSaveCliente: (ClienteCadastroData) -> Unit = {},
    onSaveCompraFornecedor: (CompraFornecedorCadastroData) -> Unit = {},
    onSaveDesconto: (DescontoCadastroData) -> Unit = {},
    onSaveEstoque: (EstoqueCadastroData) -> Unit = {},
    onSaveFuncionario: (FuncionarioCadastroData) -> Unit = {},
    onSaveFormaPagamento: (FormaPagamentoCadastroData) -> Unit = {},
    onSaveFornecedor: (FornecedorCadastroData) -> Unit = {},
    onSavePedido: (PedidoCadastroData) -> Unit = {},
) {
    val httpClient = remember { createHttpClient() }
    val principalViewModel = viewModel { PrincipalViewModel() }
    val categoriaViewModel = viewModel { CategoriaViewModel(CategoriaRepositorioRemoto(httpClient)) }
    val produtoViewModel = viewModel { ProdutoViewModel(ProdutoRepositorioRemoto(httpClient)) }

    var descontos by remember { mutableStateOf(emptyList<DescontoItem>()) }
    var estoques by remember { mutableStateOf(emptyList<EstoqueItem>()) }
    var toastNotification by remember { mutableStateOf<ToastNotificationData?>(null) }
    var isCatalogoVisible by remember { mutableStateOf(false) }
    var isAlertaCadastroVisible by remember { mutableStateOf(false) }
    var isEspecieCadastroVisible by remember { mutableStateOf(false) }
    var isClienteCadastroVisible by remember { mutableStateOf(false) }
    var isCompraFornecedorCadastroVisible by remember { mutableStateOf(false) }
    var isFuncionarioCadastroVisible by remember { mutableStateOf(false) }
    var isFormaPagamentoCadastroVisible by remember { mutableStateOf(false) }
    var isFornecedorCadastroVisible by remember { mutableStateOf(false) }
    var isPedidoCadastroVisible by remember { mutableStateOf(false) }
    fun closeScreens() {
        isCatalogoVisible = false
        isAlertaCadastroVisible = false
        isEspecieCadastroVisible = false
        isClienteCadastroVisible = false
        isCompraFornecedorCadastroVisible = false
        isFuncionarioCadastroVisible = false
        isFormaPagamentoCadastroVisible = false
        isFornecedorCadastroVisible = false
        isPedidoCadastroVisible = false
    }

    fun openCatalogo(modulo: ModulosRotas) {
        principalViewModel.abrirModulo(modulo)
        isCatalogoVisible = true
    }

    fun openDestination(destination: HomeDestination) {
        closeScreens()
        when (destination) {
            HomeDestination.CATEGORIAS -> openCatalogo(ModulosRotas.categorias)
            HomeDestination.PRODUTOS -> openCatalogo(ModulosRotas.produtos)
            HomeDestination.DESCONTOS -> openCatalogo(ModulosRotas.descontos)
            HomeDestination.ESTOQUE -> openCatalogo(ModulosRotas.estoque)
            HomeDestination.ALERTAS -> isAlertaCadastroVisible = true
            HomeDestination.ESPECIES -> isEspecieCadastroVisible = true
            HomeDestination.CLIENTES -> isClienteCadastroVisible = true
            HomeDestination.COMPRAS -> isCompraFornecedorCadastroVisible = true
            HomeDestination.FUNCIONARIOS -> isFuncionarioCadastroVisible = true
            HomeDestination.FORMAS_PAGAMENTO -> isFormaPagamentoCadastroVisible = true
            HomeDestination.FORNECEDORES -> isFornecedorCadastroVisible = true
            HomeDestination.PEDIDOS -> isPedidoCadastroVisible = true
        }
    }

    fun salvarDesconto(
        descontoId: Long?,
        data: DescontoCadastroData,
    ) {
        onSaveDesconto(data)
        val id = descontoId ?: ((descontos.maxOfOrNull { it.id } ?: 0L) + 1L)
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
            if (descontoId == null) {
                descontos + item
            } else {
                descontos.map { if (it.id == id) item else it }
            }
        toastNotification =
            ToastNotificationData(
                message =
                    if (descontoId == null) {
                        "Desconto cadastrado com sucesso."
                    } else {
                        "Desconto atualizado com sucesso."
                    },
                type = ToastNotificationType.SUCCESS,
            )
    }

    fun inativarDesconto(desconto: DescontoItem) {
        descontos = descontos.map { if (it.id == desconto.id) it.copy(ativo = false) else it }
        toastNotification =
            ToastNotificationData(
                message = "Desconto inativado com sucesso.",
                type = ToastNotificationType.DESTRUCTIVE,
            )
    }

    fun salvarEstoque(
        produtoId: Long?,
        data: EstoqueCadastroData,
    ) {
        onSaveEstoque(data)
        val estoqueAtual = estoques.firstOrNull { it.produtoId == produtoId }
        val quantidadeReservada = estoqueAtual?.quantidadeReservada ?: 0
        val item =
            EstoqueItem(
                produtoId = data.produtoId,
                quantidadeFisica = data.quantidadeFisica,
                quantidadeReservada = quantidadeReservada,
                quantidadeDisponivel = data.quantidadeFisica - quantidadeReservada,
                quantidadeMinima = data.quantidadeMinima,
                localizacao = data.localizacao,
                bloqueado = data.bloqueado,
            )
        estoques =
            if (produtoId == null) {
                estoques + item
            } else {
                estoques.map { if (it.produtoId == produtoId) item else it }
            }
        toastNotification =
            ToastNotificationData(
                message =
                    if (produtoId == null) {
                        "Estoque cadastrado com sucesso."
                    } else {
                        "Estoque atualizado com sucesso."
                    },
                type = ToastNotificationType.SUCCESS,
            )
    }

    fun excluirEstoque(estoque: EstoqueItem) {
        estoques = estoques.filterNot { it.produtoId == estoque.produtoId }
        toastNotification =
            ToastNotificationData(
                message = "Estoque excluído com sucesso.",
                type = ToastNotificationType.DESTRUCTIVE,
            )
    }

    GreenhouseTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                isCatalogoVisible -> {
                    CatalogoScreen(
                        principalViewModel = principalViewModel,
                        categoriaViewModel = categoriaViewModel,
                        produtoViewModel = produtoViewModel,
                        descontos = descontos,
                        estoques = estoques,
                        onSaveDesconto = ::salvarDesconto,
                        onDeleteDesconto = ::inativarDesconto,
                        onSaveEstoque = ::salvarEstoque,
                        onDeleteEstoque = ::excluirEstoque,
                        onVoltarHome = { isCatalogoVisible = false },
                    )
                }
                isAlertaCadastroVisible -> {
                    CadastrarAlertaIotScreen(
                        onBackClick = { isAlertaCadastroVisible = false },
                        onSalvarClick = { _, _, _, _, _, _, _, _ -> isAlertaCadastroVisible = false },
                    )
                }
                isEspecieCadastroVisible -> {
                    CadastrarEspecieScreen(
                        onBackClick = { isEspecieCadastroVisible = false },
                        onSalvarClick = { _ -> isEspecieCadastroVisible = false },
                    )
                }
                isClienteCadastroVisible -> {
                    ClienteCadastro(
                        onBack = { isClienteCadastroVisible = false },
                        onSave = onSaveCliente,
                    )
                }
                isCompraFornecedorCadastroVisible -> {
                    CompraFornecedorCadastro(
                        onBack = { isCompraFornecedorCadastroVisible = false },
                        onSave = onSaveCompraFornecedor,
                    )
                }
                isFuncionarioCadastroVisible -> {
                    FuncionarioCadastro(
                        onBack = { isFuncionarioCadastroVisible = false },
                        onSave = onSaveFuncionario,
                    )
                }
                isFormaPagamentoCadastroVisible -> {
                    FormaPagamentoCadastro(
                        onBack = { isFormaPagamentoCadastroVisible = false },
                        onSave = onSaveFormaPagamento,
                    )
                }
                isFornecedorCadastroVisible -> {
                    FornecedorCadastro(
                        onBack = { isFornecedorCadastroVisible = false },
                        onSave = onSaveFornecedor,
                    )
                }
                isPedidoCadastroVisible -> {
                    PedidoCadastro(
                        onBack = { isPedidoCadastroVisible = false },
                        onSave = onSavePedido,
                    )
                }
                else -> HomeScreen(onNavigate = ::openDestination)
            }
            ToastNotification(
                notification = toastNotification,
                onDismiss = { toastNotification = null },
                modifier = Modifier.align(Alignment.TopEnd).padding(top = 16.dp, end = 16.dp, start = 16.dp),
            )
        }
    }
}
