@file:Suppress("ktlint:standard:function-naming")

package org.greenhouse.ui.screens.catalogo

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import greenhouse.shared.generated.resources.Res
import greenhouse.shared.generated.resources.icon_categoria
import greenhouse.shared.generated.resources.icon_desconto
import greenhouse.shared.generated.resources.icon_estoque
import greenhouse.shared.generated.resources.icon_home
import greenhouse.shared.generated.resources.icon_produto
import greenhouse.shared.generated.resources.icon_voltar
import kotlinx.coroutines.launch
import org.greenhouse.app.viewmodel.CategoriaViewModel
import org.greenhouse.app.viewmodel.PrincipalViewModel
import org.greenhouse.app.viewmodel.ProdutoViewModel
import org.greenhouse.ui.components.FormularioListagemBottomBar
import org.greenhouse.ui.components.MostrarMensagem
import org.greenhouse.ui.navigation.CategoriaRotas
import org.greenhouse.ui.navigation.DescontoRotas
import org.greenhouse.ui.navigation.EstoqueRotas
import org.greenhouse.ui.navigation.ModulosRotas
import org.greenhouse.ui.navigation.ProdutoRotas
import org.greenhouse.ui.screens.categoria.CategoriaFormulario
import org.greenhouse.ui.screens.categoria.CategoriaListagem
import org.greenhouse.ui.screens.desconto.DescontoEdicao
import org.greenhouse.ui.screens.desconto.DescontoLista
import org.greenhouse.ui.screens.desconto.model.DescontoCadastroData
import org.greenhouse.ui.screens.desconto.model.DescontoItem
import org.greenhouse.ui.screens.estoque.EstoqueEdicao
import org.greenhouse.ui.screens.estoque.EstoqueLista
import org.greenhouse.ui.screens.estoque.model.EstoqueCadastroData
import org.greenhouse.ui.screens.estoque.model.EstoqueItem
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
    descontos: List<DescontoItem>,
    estoques: List<EstoqueItem>,
    onSaveDesconto: (Long?, DescontoCadastroData) -> Unit,
    onDeleteDesconto: (DescontoItem) -> Unit,
    onSaveEstoque: (Long?, EstoqueCadastroData) -> Unit,
    onDeleteEstoque: (EstoqueItem) -> Unit,
    onVoltarHome: () -> Unit,
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val descontosBackStack = remember { NavBackStack<DescontoRotas>(DescontoRotas.listagem) }
    val estoqueBackStack = remember { NavBackStack<EstoqueRotas>(EstoqueRotas.listagem) }
    MostrarMensagem(snackbar, categoriaViewModel.mensagem)
    MostrarMensagem(snackbar, produtoViewModel.mensagem)

    val moduloAtual = principalViewModel.backStackModulos.last()
    val emCategorias = moduloAtual == ModulosRotas.categorias
    val emProdutos = moduloAtual == ModulosRotas.produtos
    val emDescontos = moduloAtual == ModulosRotas.descontos
    val emListagem =
        when (moduloAtual) {
            ModulosRotas.categorias -> categoriaViewModel.backStack.last() == CategoriaRotas.listagem
            ModulosRotas.produtos -> produtoViewModel.backStack.last() == ProdutoRotas.listagem
            ModulosRotas.descontos -> descontosBackStack.last() == DescontoRotas.listagem
            ModulosRotas.estoque -> estoqueBackStack.last() == EstoqueRotas.listagem
        }
    val tituloModulo =
        when (moduloAtual) {
            ModulosRotas.categorias -> "Gestão de Categorias"
            ModulosRotas.produtos -> "Gestão de Produtos"
            ModulosRotas.descontos -> "Gestão de Descontos"
            ModulosRotas.estoque -> "Gestão de Estoque"
        }

    fun abrirDescontoFormulario(descontoId: Long? = null) {
        descontosBackStack.clear()
        descontosBackStack.add(DescontoRotas.formulario(descontoId))
    }

    fun abrirEstoqueFormulario(produtoId: Long? = null) {
        estoqueBackStack.clear()
        estoqueBackStack.add(EstoqueRotas.formulario(produtoId))
    }

    fun voltarParaDescontoListagem() {
        descontosBackStack.clear()
        descontosBackStack.add(DescontoRotas.listagem)
    }

    fun voltarParaEstoqueListagem() {
        estoqueBackStack.clear()
        estoqueBackStack.add(EstoqueRotas.listagem)
    }

    fun abrirListagemAtual() {
        when (moduloAtual) {
            ModulosRotas.categorias -> categoriaViewModel.irParaListagem()
            ModulosRotas.produtos -> produtoViewModel.irParaListagem()
            ModulosRotas.descontos -> voltarParaDescontoListagem()
            ModulosRotas.estoque -> voltarParaEstoqueListagem()
        }
    }

    fun abrirFormularioAtual() {
        when (moduloAtual) {
            ModulosRotas.categorias -> categoriaViewModel.irParaFormulario()
            ModulosRotas.produtos -> produtoViewModel.irParaFormulario()
            ModulosRotas.descontos -> abrirDescontoFormulario()
            ModulosRotas.estoque -> abrirEstoqueFormulario()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = Background) {
                Text(
                    "Módulos do catálogo",
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
                                    emListagem -> onVoltarHome()
                                    emCategorias -> categoriaViewModel.irParaListagem()
                                    emProdutos -> produtoViewModel.irParaListagem()
                                    emDescontos -> voltarParaDescontoListagem()
                                    else -> voltarParaEstoqueListagem()
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
                    formularioSelecionado = !emListagem,
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
                                backStack = descontosBackStack,
                                descontos = descontos,
                                onSave = onSaveDesconto,
                                onDelete = onDeleteDesconto,
                            )
                        }
                        entry<ModulosRotas.estoque> {
                            EstoquesScreen(
                                padding = paddingIt,
                                backStack = estoqueBackStack,
                                estoques = estoques,
                                onSave = onSaveEstoque,
                                onDelete = onDeleteEstoque,
                            )
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
    backStack: NavBackStack<DescontoRotas>,
    descontos: List<DescontoItem>,
    onSave: (Long?, DescontoCadastroData) -> Unit,
    onDelete: (DescontoItem) -> Unit,
) {
    fun abrirFormulario(descontoId: Long? = null) {
        backStack.clear()
        backStack.add(DescontoRotas.formulario(descontoId))
    }

    fun abrirListagem() {
        backStack.clear()
        backStack.add(DescontoRotas.listagem)
    }

    NavDisplay(
        backStack = backStack,
        entryProvider =
            entryProvider<NavKey> {
                entry<DescontoRotas.listagem> {
                    DescontoLista(
                        descontos = descontos,
                        onBack = ::abrirListagem,
                        onCreate = { abrirFormulario() },
                        onEdit = { abrirFormulario(it.id) },
                        onDelete = onDelete,
                        padding = padding,
                        embedded = true,
                    )
                }
                entry<DescontoRotas.formulario> { route ->
                    DescontoEdicao(
                        desconto = descontos.firstOrNull { it.id == route.descontoId },
                        onBack = ::abrirListagem,
                        onSave = { data ->
                            onSave(route.descontoId, data)
                            abrirListagem()
                        },
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
    backStack: NavBackStack<EstoqueRotas>,
    estoques: List<EstoqueItem>,
    onSave: (Long?, EstoqueCadastroData) -> Unit,
    onDelete: (EstoqueItem) -> Unit,
) {
    fun abrirFormulario(produtoId: Long? = null) {
        backStack.clear()
        backStack.add(EstoqueRotas.formulario(produtoId))
    }

    fun abrirListagem() {
        backStack.clear()
        backStack.add(EstoqueRotas.listagem)
    }

    NavDisplay(
        backStack = backStack,
        entryProvider =
            entryProvider<NavKey> {
                entry<EstoqueRotas.listagem> {
                    EstoqueLista(
                        estoques = estoques,
                        onBack = ::abrirListagem,
                        onCreate = { abrirFormulario() },
                        onEdit = { abrirFormulario(it.produtoId) },
                        onDelete = onDelete,
                        padding = padding,
                        embedded = true,
                    )
                }
                entry<EstoqueRotas.formulario> { route ->
                    val estoque = estoques.firstOrNull { it.produtoId == route.produtoId }
                    EstoqueEdicao(
                        estoque = estoque,
                        onBack = ::abrirListagem,
                        onSave = { data ->
                            onSave(route.produtoId, data)
                            abrirListagem()
                        },
                        existingProdutoIds = estoques.map { it.produtoId }.toSet(),
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
