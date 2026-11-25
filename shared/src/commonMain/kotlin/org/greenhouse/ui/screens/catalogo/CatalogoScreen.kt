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
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import greenhouse.shared.generated.resources.Res
import greenhouse.shared.generated.resources.icon_categoria
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
import org.greenhouse.ui.navigation.ModulosRotas
import org.greenhouse.ui.navigation.ProdutoRotas
import org.greenhouse.ui.screens.categoria.CategoriaFormulario
import org.greenhouse.ui.screens.categoria.CategoriaListagem
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
    onVoltarHome: () -> Unit,
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    MostrarMensagem(snackbar, categoriaViewModel.mensagem)
    MostrarMensagem(snackbar, produtoViewModel.mensagem)

    val moduloAtual = principalViewModel.backStackModulos.last()
    val emCategorias = moduloAtual == ModulosRotas.categorias
    val emListagem =
        if (emCategorias) {
            categoriaViewModel.backStack.last() == CategoriaRotas.listagem
        } else {
            produtoViewModel.backStack.last() == ProdutoRotas.listagem
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
                ItemGaveta("Produtos", Res.drawable.icon_produto, !emCategorias) {
                    principalViewModel.abrirModulo(ModulosRotas.produtos)
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
                    title = { Text(if (emCategorias) "Gestão de Categorias" else "Gestão de Produtos") },
                    actions = {
                        IconButton(
                            onClick = {
                                when {
                                    emListagem -> onVoltarHome()
                                    emCategorias -> categoriaViewModel.irParaListagem()
                                    else -> produtoViewModel.irParaListagem()
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
                if (emCategorias) {
                    FormularioListagemBottomBar(
                        formularioSelecionado = !emListagem,
                        onFormulario = { categoriaViewModel.irParaFormulario() },
                        onListagem = { categoriaViewModel.irParaListagem() },
                    )
                } else {
                    FormularioListagemBottomBar(
                        formularioSelecionado = !emListagem,
                        onFormulario = { produtoViewModel.irParaFormulario() },
                        onListagem = { produtoViewModel.irParaListagem() },
                    )
                }
            },
            snackbarHost = { SnackbarHost(snackbar) },
        ) { paddingIt ->
            NavDisplay(
                backStack = principalViewModel.backStackModulos,
                entryProvider =
                    entryProvider<NavKey> {
                        entry<ModulosRotas.categorias> { CategoriasScreen(paddingIt, categoriaViewModel) }
                        entry<ModulosRotas.produtos> { ProdutosScreen(paddingIt, produtoViewModel) }
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
