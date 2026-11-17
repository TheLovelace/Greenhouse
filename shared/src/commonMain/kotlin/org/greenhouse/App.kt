@file:Suppress("ktlint:standard:function-naming")

package org.greenhouse

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import org.greenhouse.app.repository.CategoriaRepositorioRemoto
import org.greenhouse.app.repository.ProdutoRepositorioRemoto
import org.greenhouse.app.repository.createHttpClient
import org.greenhouse.app.viewmodel.CategoriaViewModel
import org.greenhouse.app.viewmodel.PrincipalViewModel
import org.greenhouse.app.viewmodel.ProdutoViewModel
import org.greenhouse.ui.navigation.ModulosRotas
import org.greenhouse.ui.screens.alerta.CadastrarAlertaIotScreen
import org.greenhouse.ui.screens.catalogo.CatalogoScreen
import org.greenhouse.ui.screens.cliente.ClienteCadastro
import org.greenhouse.ui.screens.cliente.model.ClienteCadastroData
import org.greenhouse.ui.screens.compraFornecedor.CompraFornecedorCadastro
import org.greenhouse.ui.screens.compraFornecedor.model.CompraFornecedorCadastroData
import org.greenhouse.ui.screens.desconto.DescontoCadastro
import org.greenhouse.ui.screens.desconto.model.DescontoCadastroData
import org.greenhouse.ui.screens.especie.CadastrarEspecieScreen
import org.greenhouse.ui.screens.estoque.EstoqueCadastro
import org.greenhouse.ui.screens.estoque.model.EstoqueCadastroData
import org.greenhouse.ui.screens.funcionario.FuncionarioCadastro
import org.greenhouse.ui.screens.funcionario.model.FuncionarioCadastroData
import org.greenhouse.ui.screens.formaPagamento.FormaPagamentoCadastro
import org.greenhouse.ui.screens.formaPagamento.model.FormaPagamentoCadastroData
import org.greenhouse.ui.screens.fornecedor.FornecedorCadastro
import org.greenhouse.ui.screens.fornecedor.model.FornecedorCadastroData
import org.greenhouse.ui.screens.home.HomeScreen
import org.greenhouse.ui.screens.home.HomeDestination
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

    var isCatalogoVisible by remember { mutableStateOf(false) }
    var isAlertaCadastroVisible by remember { mutableStateOf(false) }
    var isEspecieCadastroVisible by remember { mutableStateOf(false) }
    var isClienteCadastroVisible by remember { mutableStateOf(false) }
    var isCompraFornecedorCadastroVisible by remember { mutableStateOf(false) }
    var isDescontoCadastroVisible by remember { mutableStateOf(false) }
    var isEstoqueCadastroVisible by remember { mutableStateOf(false) }
    var isFuncionarioCadastroVisible by remember { mutableStateOf(false) }
    var isFormaPagamentoCadastroVisible by remember { mutableStateOf(false) }
    var isFornecedorCadastroVisible by remember { mutableStateOf(false) }
    var isPedidoCadastroVisible by remember { mutableStateOf(false) }

    GreenhouseTheme {
        if (isCatalogoVisible) {
            CatalogoScreen(
                principalViewModel = principalViewModel,
                categoriaViewModel = categoriaViewModel,
                produtoViewModel = produtoViewModel,
                onVoltarHome = { isCatalogoVisible = false },
            )
        } else if (isClienteCadastroVisible) {
            ClienteCadastro(
                onBack = { isClienteCadastroVisible = false },
                onSave = onSaveCliente,
            )
        } else if (isCompraFornecedorCadastroVisible) {
            CompraFornecedorCadastro(
                onBack = { isCompraFornecedorCadastroVisible = false },
                onSave = onSaveCompraFornecedor,
            )
        } else if (isDescontoCadastroVisible) {
            DescontoCadastro(
                onBack = { isDescontoCadastroVisible = false },
                onSave = onSaveDesconto,
            )
        } else if (isEstoqueCadastroVisible) {
            EstoqueCadastro(
                onBack = { isEstoqueCadastroVisible = false },
                onSave = onSaveEstoque,
            )
        } else if (isFuncionarioCadastroVisible) {
            FuncionarioCadastro(
                onBack = { isFuncionarioCadastroVisible = false },
                onSave = onSaveFuncionario,
            )
        } else if (isFormaPagamentoCadastroVisible) {
            FormaPagamentoCadastro(
                onBack = { isFormaPagamentoCadastroVisible = false },
                onSave = onSaveFormaPagamento,
            )
        } else if (isFornecedorCadastroVisible) {
            FornecedorCadastro(
                onBack = { isFornecedorCadastroVisible = false },
                onSave = onSaveFornecedor,
            )
        } else if (isPedidoCadastroVisible) {
            PedidoCadastro(
                onBack = { isPedidoCadastroVisible = false },
                onSave = onSavePedido,
            )
        } else if (isAlertaCadastroVisible) {
            CadastrarAlertaIotScreen(
                onBackClick = { isAlertaCadastroVisible = false },
                onSalvarClick = { _, _, _, _, _, _, _, _ -> isAlertaCadastroVisible = false },
            )
        } else if (isEspecieCadastroVisible) {
            CadastrarEspecieScreen(
                onBackClick = { isEspecieCadastroVisible = false },
                onSalvarClick = { _ -> isEspecieCadastroVisible = false },
            )
        } else {
            HomeScreen(
                onNavigate = { destination ->
                    when (destination) {
                        HomeDestination.CATEGORIAS -> {
                            principalViewModel.abrirModulo(ModulosRotas.categorias)
                            isCatalogoVisible = true
                        }
                        HomeDestination.CLIENTES -> isClienteCadastroVisible = true
                        HomeDestination.COMPRAS -> isCompraFornecedorCadastroVisible = true
                        HomeDestination.DESCONTOS -> isDescontoCadastroVisible = true
                        HomeDestination.ESTOQUE -> isEstoqueCadastroVisible = true
                        HomeDestination.FUNCIONARIOS -> isFuncionarioCadastroVisible = true
                        HomeDestination.FORMAS_PAGAMENTO -> isFormaPagamentoCadastroVisible = true
                        HomeDestination.FORNECEDORES -> isFornecedorCadastroVisible = true
                        HomeDestination.PEDIDOS -> isPedidoCadastroVisible = true
                        HomeDestination.PRODUTOS -> {
                            principalViewModel.abrirModulo(ModulosRotas.produtos)
                            isCatalogoVisible = true
                        }
                        HomeDestination.ESPECIES -> isEspecieCadastroVisible = true
                        HomeDestination.ALERTAS -> isAlertaCadastroVisible = true
                        else -> onNavigate(destination)
                    }
                },
            )
        }
    }
}