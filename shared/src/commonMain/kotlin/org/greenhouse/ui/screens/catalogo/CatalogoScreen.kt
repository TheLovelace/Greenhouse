@file:Suppress("ktlint:standard:function-naming")

package org.greenhouse.ui.screens.catalogo

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import greenhouse.shared.generated.resources.Res
import greenhouse.shared.generated.resources.icon_categoria
import greenhouse.shared.generated.resources.icon_desconto
import greenhouse.shared.generated.resources.icon_estoque
import greenhouse.shared.generated.resources.icon_compra
import greenhouse.shared.generated.resources.icon_forma_pagamento
import greenhouse.shared.generated.resources.icon_fornecedor
import greenhouse.shared.generated.resources.icon_funcionario
import greenhouse.shared.generated.resources.icon_home
import greenhouse.shared.generated.resources.icon_produto
import greenhouse.shared.generated.resources.icon_voltar
import kotlinx.coroutines.launch
import org.greenhouse.app.viewmodel.CategoriaViewModel
import org.greenhouse.app.viewmodel.DescontoViewModel
import org.greenhouse.app.viewmodel.EstoqueViewModel
import org.greenhouse.app.viewmodel.CompraFornecedorViewModel
import org.greenhouse.app.viewmodel.FormaPagamentoViewModel
import org.greenhouse.app.viewmodel.FornecedorViewModel
import org.greenhouse.app.viewmodel.FuncionarioViewModel
import org.greenhouse.app.viewmodel.PrincipalViewModel
import org.greenhouse.app.viewmodel.ProdutoViewModel
import org.greenhouse.ui.components.FormularioListagemBottomBar
import org.greenhouse.ui.components.MostrarMensagem
import org.greenhouse.ui.components.ToastNotificationType
import org.greenhouse.ui.screens.compraFornecedor.CompraFornecedorGestaoScreen
import org.greenhouse.ui.screens.compraFornecedor.model.CompraFornecedorCadastroData
import org.greenhouse.ui.screens.compraFornecedor.model.StatusCompraCadastro
import org.greenhouse.ui.navigation.CategoriaRotas
import org.greenhouse.ui.navigation.DescontoRotas
import org.greenhouse.ui.navigation.EstoqueRotas
import org.greenhouse.ui.navigation.ModulosRotas
import org.greenhouse.ui.navigation.ProdutoRotas
import org.greenhouse.ui.screens.formaPagamento.FormaPagamentoGestaoScreen
import org.greenhouse.ui.screens.formaPagamento.model.FormaPagamentoCadastroData
import org.greenhouse.ui.screens.fornecedor.FornecedorGestaoScreen
import org.greenhouse.ui.screens.fornecedor.model.FornecedorCadastroData
import org.greenhouse.ui.screens.funcionario.FuncionarioGestaoScreen
import org.greenhouse.ui.screens.funcionario.model.FuncionarioCadastroData
import org.greenhouse.ui.screens.categoria.CategoriaFormulario
import org.greenhouse.ui.screens.categoria.CategoriaListagem
import org.greenhouse.ui.screens.desconto.DescontoEdicao
import org.greenhouse.ui.screens.desconto.DescontoLista
import org.greenhouse.ui.screens.estoque.EstoqueEdicao
import org.greenhouse.ui.screens.estoque.EstoqueLista
import org.greenhouse.ui.screens.produto.ProdutoFormulario
import org.greenhouse.ui.screens.produto.ProdutoListagem
import org.greenhouse.ui.theme.AppTypography
import org.greenhouse.ui.theme.Background
import org.greenhouse.ui.theme.GreenPrimary
import org.greenhouse.ui.theme.GreenSecondary
import org.greenhouse.ui.theme.Surface
import org.greenhouse.ui.theme.TextPrimary
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogoScreen(
    principalViewModel: PrincipalViewModel,
    categoriaViewModel: CategoriaViewModel,
    produtoViewModel: ProdutoViewModel,
    descontoViewModel: DescontoViewModel,
    estoqueViewModel: EstoqueViewModel,
    funcionarioViewModel: FuncionarioViewModel,
    fornecedorViewModel: FornecedorViewModel,
    formaPagamentoViewModel: FormaPagamentoViewModel,
    compraFornecedorViewModel: CompraFornecedorViewModel,
    onShowToast: (String, ToastNotificationType) -> Unit,
    onSaveFuncionario: (FuncionarioCadastroData) -> Unit,
    onSaveFornecedor: (FornecedorCadastroData) -> Unit,
    onSaveFormaPagamento: (FormaPagamentoCadastroData) -> Unit,
    onSaveCompraFornecedor: (CompraFornecedorCadastroData) -> Unit,
    onVoltarHome: () -> Unit,
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    MostrarMensagem(snackbar, categoriaViewModel.mensagem)
    MostrarMensagem(snackbar, produtoViewModel.mensagem)
    MostrarMensagem(snackbar, descontoViewModel.mensagem)
    MostrarMensagem(snackbar, estoqueViewModel.mensagem)

    val moduloAtual = principalViewModel.backStackModulos.last()
    val emCategorias = moduloAtual == ModulosRotas.categorias
    val emProdutos = moduloAtual == ModulosRotas.produtos
    val emDescontos = moduloAtual == ModulosRotas.descontos
    val emFuncionarios = moduloAtual == ModulosRotas.funcionarios
    val emFornecedores = moduloAtual == ModulosRotas.fornecedores
    val emFormasPagamento = moduloAtual == ModulosRotas.formasPagamento
    val emCompras = moduloAtual == ModulosRotas.compras
    val emListagem =
        when (moduloAtual) {
            ModulosRotas.categorias -> categoriaViewModel.backStack.last() == CategoriaRotas.listagem
            ModulosRotas.produtos -> produtoViewModel.backStack.last() == ProdutoRotas.listagem
            ModulosRotas.descontos -> descontoViewModel.backStack.last() == DescontoRotas.listagem
            ModulosRotas.estoque -> estoqueViewModel.backStack.last() == EstoqueRotas.listagem
            ModulosRotas.funcionarios -> !funcionarioViewModel.exibindoFormulario.value
            ModulosRotas.fornecedores -> !fornecedorViewModel.exibindoFormulario
            ModulosRotas.formasPagamento -> !formaPagamentoViewModel.exibindoFormulario.value
            ModulosRotas.compras -> !compraFornecedorViewModel.exibindoFormulario
        }
    val emListagemPrincipal =
        emListagem &&
            !(emCompras && compraFornecedorViewModel.compraSelecionada != null)
    val formularioSelecionado =
        when (moduloAtual) {
            ModulosRotas.categorias -> categoriaViewModel.backStack.last() == CategoriaRotas.formulario
            ModulosRotas.produtos -> produtoViewModel.backStack.last() == ProdutoRotas.formulario
            ModulosRotas.descontos -> descontoViewModel.backStack.last() is DescontoRotas.formulario
            ModulosRotas.estoque -> estoqueViewModel.backStack.last() is EstoqueRotas.formulario
            ModulosRotas.funcionarios -> funcionarioViewModel.exibindoFormulario.value
            ModulosRotas.fornecedores -> fornecedorViewModel.exibindoFormulario
            ModulosRotas.formasPagamento -> formaPagamentoViewModel.exibindoFormulario.value
            ModulosRotas.compras -> compraFornecedorViewModel.exibindoFormulario
        }
    val tituloModulo =
        when (moduloAtual) {
            ModulosRotas.categorias -> "Gestão de Categorias"
            ModulosRotas.produtos -> "Gestão de Produtos"
            ModulosRotas.descontos -> "Gestão de Descontos"
            ModulosRotas.estoque -> "Gestão de Estoque"
            ModulosRotas.funcionarios -> "Gestão de Funcionários"
            ModulosRotas.fornecedores -> "Gestão de Fornecedores"
            ModulosRotas.formasPagamento -> "Gestão de Formas de Pagamento"
            ModulosRotas.compras -> "Gestão de Compras"
        }

    fun abrirListagemAtual() {
        when (moduloAtual) {
            ModulosRotas.categorias -> categoriaViewModel.irParaListagem()
            ModulosRotas.produtos -> produtoViewModel.irParaListagem()
            ModulosRotas.descontos -> descontoViewModel.irParaListagem()
            ModulosRotas.estoque -> estoqueViewModel.irParaListagem()
            ModulosRotas.funcionarios -> funcionarioViewModel.voltarParaListagem()
            ModulosRotas.fornecedores -> fornecedorViewModel.voltarParaListagem()
            ModulosRotas.formasPagamento -> formaPagamentoViewModel.voltarParaListagem()
            ModulosRotas.compras -> compraFornecedorViewModel.voltarParaListagem()
        }
    }

    fun abrirFormularioAtual() {
        when (moduloAtual) {
            ModulosRotas.categorias -> categoriaViewModel.irParaFormulario()
            ModulosRotas.produtos -> produtoViewModel.irParaFormulario()
            ModulosRotas.descontos -> descontoViewModel.novo()
            ModulosRotas.estoque -> estoqueViewModel.novo()
            ModulosRotas.funcionarios -> if (!funcionarioViewModel.exibindoFormulario.value) funcionarioViewModel.novo()
            ModulosRotas.fornecedores -> if (!fornecedorViewModel.exibindoFormulario) fornecedorViewModel.novo()
            ModulosRotas.formasPagamento -> if (!formaPagamentoViewModel.exibindoFormulario.value) formaPagamentoViewModel.novo()
            ModulosRotas.compras -> {
                if (!compraFornecedorViewModel.exibindoFormulario) {
                    if (compraFornecedorViewModel.compraSelecionada == null) {
                        compraFornecedorViewModel.novo()
                    } else if (compraFornecedorViewModel.compraSelecionada?.status == StatusCompraCadastro.PEDIDA) {
                        compraFornecedorViewModel.editar()
                    }
                }
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = Background) {
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    Text(
                        "Módulos do sistema",
                        color = TextPrimary,
                        style = AppTypography().titleMedium,
                        modifier = Modifier.padding(16.dp),
                    )
                    ItemGaveta("Categorias", Res.drawable.icon_categoria, emCategorias) {
                        principalViewModel.abrirModulo(ModulosRotas.categorias)
                        scope.launch { drawerState.close() }
                    }
                    ItemGaveta("Produtos", Res.drawable.icon_produto, emProdutos) {
                        principalViewModel.abrirModulo(ModulosRotas.produtos)
                        scope.launch { drawerState.close() }
                    }
                    ItemGaveta("Descontos", Res.drawable.icon_desconto, emDescontos) {
                        principalViewModel.abrirModulo(ModulosRotas.descontos)
                        scope.launch { drawerState.close() }
                    }
                    ItemGaveta("Estoque", Res.drawable.icon_estoque, moduloAtual == ModulosRotas.estoque) {
                        principalViewModel.abrirModulo(ModulosRotas.estoque)
                        scope.launch { drawerState.close() }
                    }
                    ItemGaveta("Funcionários", Res.drawable.icon_funcionario, emFuncionarios) {
                        principalViewModel.abrirModulo(ModulosRotas.funcionarios)
                        scope.launch { drawerState.close() }
                    }
                    ItemGaveta("Fornecedores", Res.drawable.icon_fornecedor, emFornecedores) {
                        principalViewModel.abrirModulo(ModulosRotas.fornecedores)
                        scope.launch { drawerState.close() }
                    }
                    ItemGaveta("Formas de pagamento", Res.drawable.icon_forma_pagamento, emFormasPagamento) {
                        principalViewModel.abrirModulo(ModulosRotas.formasPagamento)
                        scope.launch { drawerState.close() }
                    }
                    ItemGaveta("Compras", Res.drawable.icon_compra, emCompras) {
                        principalViewModel.abrirModulo(ModulosRotas.compras)
                        scope.launch { drawerState.close() }
                    }
                }
            }
        },
    ) {
        Scaffold(
            containerColor = Background,
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface, titleContentColor = TextPrimary),
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Text("☰", color = GreenPrimary, style = AppTypography().titleLarge)
                        }
                    },
                    title = { Text(tituloModulo) },
                    actions = {
                        IconButton(
                            onClick = {
                                when {
                                    emListagemPrincipal -> onVoltarHome()
                                    emCategorias -> categoriaViewModel.irParaListagem()
                                    emProdutos -> produtoViewModel.irParaListagem()
                                    emDescontos -> descontoViewModel.irParaListagem()
                                    emFuncionarios -> funcionarioViewModel.voltarParaListagem()
                                    emFornecedores -> fornecedorViewModel.voltarParaListagem()
                                    emFormasPagamento -> formaPagamentoViewModel.voltarParaListagem()
                                    emCompras -> compraFornecedorViewModel.voltarParaListagem()
                                    else -> estoqueViewModel.irParaListagem()
                                }
                            },
                        ) {
                            Image(painterResource(Res.drawable.icon_voltar), contentDescription = "Voltar", modifier = Modifier.size(28.dp))
                        }
                        IconButton(onClick = onVoltarHome) {
                            Image(painterResource(Res.drawable.icon_home), contentDescription = "Início", modifier = Modifier.size(28.dp))
                        }
                    },
                )
            },
            bottomBar = {
                FormularioListagemBottomBar(
                    formularioSelecionado = formularioSelecionado,
                    onFormulario = ::abrirFormularioAtual,
                    onListagem = ::abrirListagemAtual,
                )
            },
            snackbarHost = { SnackbarHost(snackbar) },
        ) { paddingIt ->
            NavDisplay(
                backStack = principalViewModel.backStackModulos,
                entryProvider =
                    entryProvider<NavKey> {
                        entry<ModulosRotas.categorias> { CategoriasScreen(paddingIt, categoriaViewModel) }
                        entry<ModulosRotas.produtos> { ProdutosScreen(paddingIt, produtoViewModel) }
                        entry<ModulosRotas.descontos> {
                            DescontosScreen(
                                padding = paddingIt,
                                viewModel = descontoViewModel,
                                onShowToast = onShowToast,
                            )
                        }
                        entry<ModulosRotas.estoque> {
                            EstoquesScreen(
                                padding = paddingIt,
                                viewModel = estoqueViewModel,
                                onShowToast = onShowToast,
                            )
                        }
                        entry<ModulosRotas.funcionarios> {
                            Box(Modifier.fillMaxSize().padding(paddingIt)) {
                                FuncionarioGestaoScreen(
                                    viewModel = funcionarioViewModel,
                                    onBack = onVoltarHome,
                                    onAfterSave = onSaveFuncionario,
                                    onShowToast = onShowToast,
                                    embedded = true,
                                )
                            }
                        }
                        entry<ModulosRotas.fornecedores> {
                            Box(Modifier.fillMaxSize().padding(paddingIt)) {
                                FornecedorGestaoScreen(
                                    viewModel = fornecedorViewModel,
                                    onBack = onVoltarHome,
                                    onAfterSave = onSaveFornecedor,
                                    onShowToast = onShowToast,
                                    embedded = true,
                                )
                            }
                        }
                        entry<ModulosRotas.formasPagamento> {
                            Box(Modifier.fillMaxSize().padding(paddingIt)) {
                                FormaPagamentoGestaoScreen(
                                    viewModel = formaPagamentoViewModel,
                                    onBack = onVoltarHome,
                                    onAfterSave = onSaveFormaPagamento,
                                    onShowToast = onShowToast,
                                    embedded = true,
                                )
                            }
                        }
                        entry<ModulosRotas.compras> {
                            Box(Modifier.fillMaxSize().padding(paddingIt)) {
                                CompraFornecedorGestaoScreen(
                                    viewModel = compraFornecedorViewModel,
                                    onBack = onVoltarHome,
                                    onAfterSave = onSaveCompraFornecedor,
                                    onShowToast = onShowToast,
                                    embedded = true,
                                )
                            }
                        }
                    },
            )
        }
    }
}

@Composable
fun CategoriasScreen(
    paddingIt: PaddingValues,
    categoriaViewModel: CategoriaViewModel,
) {
    NavDisplay(
        backStack = categoriaViewModel.backStack,
        entryProvider =
            entryProvider<NavKey> {
                entry<CategoriaRotas.listagem> { CategoriaListagem(paddingIt, categoriaViewModel) }
                entry<CategoriaRotas.formulario> { CategoriaFormulario(paddingIt, categoriaViewModel) }
            },
    )
}

@Composable
fun ProdutosScreen(
    paddingIt: PaddingValues,
    produtoViewModel: ProdutoViewModel,
) {
    NavDisplay(
        backStack = produtoViewModel.backStack,
        entryProvider =
            entryProvider<NavKey> {
                entry<ProdutoRotas.listagem> { ProdutoListagem(paddingIt, produtoViewModel) }
                entry<ProdutoRotas.formulario> { ProdutoFormulario(paddingIt, produtoViewModel) }
            },
    )
}

@Composable
private fun DescontosScreen(
    padding: PaddingValues,
    viewModel: DescontoViewModel,
    onShowToast: (String, ToastNotificationType) -> Unit,
) {
    val descontos by viewModel.lista.collectAsState()
    LaunchedEffect(Unit) { viewModel.carregar() }

    NavDisplay(
        backStack = viewModel.backStack,
        entryProvider =
            entryProvider<NavKey> {
                entry<DescontoRotas.listagem> {
                    DescontoLista(
                        descontos = descontos,
                        onBack = viewModel::irParaListagem,
                        onCreate = viewModel::novo,
                        onEdit = viewModel::editar,
                        onDelete = { desconto ->
                            viewModel.excluir(desconto) { mensagem ->
                                onShowToast(mensagem, ToastNotificationType.DESTRUCTIVE)
                            }
                        },
                        padding = padding,
                        embedded = true,
                    )
                }
                entry<DescontoRotas.formulario> {
                    DescontoEdicao(
                        viewModel = viewModel,
                        onBack = viewModel::irParaListagem,
                        onSuccess = { mensagem -> onShowToast(mensagem, ToastNotificationType.SUCCESS) },
                        padding = padding,
                        embedded = true,
                    )
                }
            },
    )
}

@Composable
private fun EstoquesScreen(
    padding: PaddingValues,
    viewModel: EstoqueViewModel,
    onShowToast: (String, ToastNotificationType) -> Unit,
) {
    val estoques by viewModel.lista.collectAsState()
    LaunchedEffect(Unit) { viewModel.carregar() }

    NavDisplay(
        backStack = viewModel.backStack,
        entryProvider =
            entryProvider<NavKey> {
                entry<EstoqueRotas.listagem> {
                    EstoqueLista(
                        estoques = estoques,
                        onBack = viewModel::irParaListagem,
                        onCreate = viewModel::novo,
                        onEdit = viewModel::editar,
                        onDelete = { estoque ->
                            viewModel.excluir(estoque) { mensagem ->
                                onShowToast(mensagem, ToastNotificationType.DESTRUCTIVE)
                            }
                        },
                        padding = padding,
                        embedded = true,
                    )
                }
                entry<EstoqueRotas.formulario> {
                    EstoqueEdicao(
                        viewModel = viewModel,
                        onBack = viewModel::irParaListagem,
                        onSuccess = { mensagem -> onShowToast(mensagem, ToastNotificationType.SUCCESS) },
                        padding = padding,
                        embedded = true,
                    )
                }
            },
    )
}

@Composable
private fun ItemGaveta(
    texto: String,
    icone: DrawableResource,
    selecionado: Boolean,
    onClick: () -> Unit,
) {
    NavigationDrawerItem(
        label = { Text(texto, color = TextPrimary) },
        icon = { Image(painterResource(icone), contentDescription = null, modifier = Modifier.size(28.dp)) },
        selected = selecionado,
        onClick = onClick,
        colors = NavigationDrawerItemDefaults.colors(selectedContainerColor = GreenSecondary.copy(alpha = 0.3f)),
        modifier = Modifier.padding(horizontal = 12.dp),
    )
    Spacer(Modifier.height(4.dp))
}
