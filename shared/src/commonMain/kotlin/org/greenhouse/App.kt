@file:Suppress("ktlint:standard:function-naming")

package org.greenhouse

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenhouse.app.repository.AuthRepositorioRemoto
import org.greenhouse.app.repository.AuthSessionManager
import org.greenhouse.app.repository.AuthTokenStorage
import org.greenhouse.app.repository.CategoriaRepositorioRemoto
import org.greenhouse.app.repository.DescontoRepositorioRemoto
import org.greenhouse.app.repository.EstoqueRepositorioRemoto
import org.greenhouse.app.repository.FormaPagamentoRepositorioRemoto
import org.greenhouse.app.repository.FuncionarioRepositorioRemoto
import org.greenhouse.app.repository.ProdutoRepositorioRemoto
import org.greenhouse.app.repository.createAuthTokenStorage
import org.greenhouse.app.repository.createHttpClient
import org.greenhouse.app.viewmodel.AuthViewModel
import org.greenhouse.app.viewmodel.CategoriaViewModel
import org.greenhouse.app.viewmodel.DescontoViewModel
import org.greenhouse.app.viewmodel.EstoqueViewModel
import org.greenhouse.app.viewmodel.FormaPagamentoViewModel
import org.greenhouse.app.viewmodel.FuncionarioViewModel
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
import org.greenhouse.ui.screens.especie.CadastrarEspecieScreen
import org.greenhouse.ui.screens.formaPagamento.FormaPagamentoGestaoScreen
import org.greenhouse.ui.screens.formaPagamento.model.FormaPagamentoCadastroData
import org.greenhouse.ui.screens.fornecedor.FornecedorCadastro
import org.greenhouse.ui.screens.fornecedor.model.FornecedorCadastroData
import org.greenhouse.ui.screens.funcionario.FuncionarioGestaoScreen
import org.greenhouse.ui.screens.funcionario.model.FuncionarioCadastroData
import org.greenhouse.ui.screens.home.HomeDestination
import org.greenhouse.ui.screens.home.HomeScreen
import org.greenhouse.ui.screens.loading.LoadingScreen
import org.greenhouse.ui.screens.login.FirstAccessLoginScreen
import org.greenhouse.ui.screens.login.FirstAccessPasswordScreen
import org.greenhouse.ui.screens.login.LoginScreen
import org.greenhouse.ui.screens.pedido.PedidoCadastro
import org.greenhouse.ui.screens.pedido.model.PedidoCadastroData
import org.greenhouse.ui.screens.welcome.WelcomeScreen
import org.greenhouse.ui.theme.GreenhouseTheme

@Composable
fun App(
    authTokenStorage: AuthTokenStorage = createAuthTokenStorage(),
    onNavigate: (HomeDestination) -> Unit = {},
    onSaveCliente: (ClienteCadastroData) -> Unit = {},
    onSaveCompraFornecedor: (CompraFornecedorCadastroData) -> Unit = {},
    onSaveFuncionario: (FuncionarioCadastroData) -> Unit = {},
    onSaveFormaPagamento: (FormaPagamentoCadastroData) -> Unit = {},
    onSaveFornecedor: (FornecedorCadastroData) -> Unit = {},
    onSavePedido: (PedidoCadastroData) -> Unit = {},
) {
    val httpClient = remember { createHttpClient() }
    val authRepositorio = remember { AuthRepositorioRemoto(httpClient) }
    val authViewModel = viewModel { AuthViewModel(authRepositorio) }
    val authSession by AuthSessionManager.session.collectAsState()
    val authMessage by AuthSessionManager.message.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val principalViewModel = viewModel { PrincipalViewModel() }
    val categoriaViewModel = viewModel { CategoriaViewModel(CategoriaRepositorioRemoto(httpClient)) }
    val produtoViewModel = viewModel { ProdutoViewModel(ProdutoRepositorioRemoto(httpClient)) }
    val descontoViewModel = viewModel { DescontoViewModel(DescontoRepositorioRemoto(httpClient)) }
    val estoqueViewModel = viewModel { EstoqueViewModel(EstoqueRepositorioRemoto(httpClient)) }
    val funcionarioViewModel = viewModel { FuncionarioViewModel(FuncionarioRepositorioRemoto(httpClient)) }
    val formaPagamentoViewModel = viewModel { FormaPagamentoViewModel(FormaPagamentoRepositorioRemoto(httpClient)) }

    var entryScreen by remember { mutableStateOf(AppEntryScreen.LOADING) }
    var firstAccessLogin by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        AuthSessionManager.configure(authTokenStorage, authRepositorio::renovar)
        try {
            AuthSessionManager.restoreSession()
        } catch (exception: CancellationException) {
            throw exception
        } catch (_: Exception) {
            authViewModel.reportError("Não foi possível restaurar a sessão. Tente entrar novamente.")
        }
        delay(900)
        entryScreen = if (AuthSessionManager.session.value != null) AppEntryScreen.HOME else AppEntryScreen.WELCOME
    }
    LaunchedEffect(authSession) {
        when {
            authSession != null -> entryScreen = AppEntryScreen.HOME
            entryScreen == AppEntryScreen.HOME -> entryScreen = AppEntryScreen.LOGIN
        }
    }

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

    GreenhouseTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                entryScreen == AppEntryScreen.LOADING -> {
                    LoadingScreen()
                }
                entryScreen == AppEntryScreen.WELCOME -> {
                    WelcomeScreen(onLogin = { entryScreen = AppEntryScreen.LOGIN })
                }
                entryScreen == AppEntryScreen.LOGIN -> {
                    LoginScreen(
                        onBack = { entryScreen = AppEntryScreen.WELCOME },
                        onSubmit = authViewModel::entrar,
                        onFirstAccess = {
                            firstAccessLogin = ""
                            authViewModel.clearError()
                            entryScreen = AppEntryScreen.FIRST_ACCESS_LOGIN
                        },
                        isLoading = authViewModel.isLoading,
                        errorMessage = authViewModel.errorMessage ?: authMessage,
                    )
                }
                entryScreen == AppEntryScreen.FIRST_ACCESS_LOGIN -> {
                    FirstAccessLoginScreen(
                        initialLogin = firstAccessLogin,
                        onBack = {
                            authViewModel.clearError()
                            entryScreen = AppEntryScreen.LOGIN
                        },
                        onSubmit = { login ->
                            authViewModel.validarPrimeiroAcesso(login) {
                                firstAccessLogin = login.trim().substringBefore('@').lowercase()
                                entryScreen = AppEntryScreen.FIRST_ACCESS_PASSWORD
                            }
                        },
                        isLoading = authViewModel.isLoading,
                        errorMessage = authViewModel.errorMessage,
                    )
                }
                entryScreen == AppEntryScreen.FIRST_ACCESS_PASSWORD -> {
                    FirstAccessPasswordScreen(
                        login = firstAccessLogin,
                        onBack = {
                            authViewModel.clearError()
                            entryScreen = AppEntryScreen.FIRST_ACCESS_LOGIN
                        },
                        onSubmit = { cpf, email, password ->
                            authViewModel.definirSenhaPrimeiroAcesso(
                                login = firstAccessLogin,
                                cpf = cpf,
                                email = email,
                                novaSenha = password,
                            ) {
                                firstAccessLogin = ""
                                entryScreen = AppEntryScreen.LOGIN
                            }
                        },
                        isLoading = authViewModel.isLoading,
                        errorMessage = authViewModel.errorMessage,
                    )
                }
                isCatalogoVisible -> {
                    CatalogoScreen(
                        principalViewModel = principalViewModel,
                        categoriaViewModel = categoriaViewModel,
                        produtoViewModel = produtoViewModel,
                        descontoViewModel = descontoViewModel,
                        estoqueViewModel = estoqueViewModel,
                        onShowToast = { message, type ->
                            toastNotification = ToastNotificationData(message, type)
                        },
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
                    FuncionarioGestaoScreen(
                        viewModel = funcionarioViewModel,
                        onBack = { isFuncionarioCadastroVisible = false },
                        onAfterSave = onSaveFuncionario,
                        onShowToast = { message, type ->
                            toastNotification = ToastNotificationData(message, type)
                        },
                    )
                }
                isFormaPagamentoCadastroVisible -> {
                    FormaPagamentoGestaoScreen(
                        viewModel = formaPagamentoViewModel,
                        onBack = { isFormaPagamentoCadastroVisible = false },
                        onAfterSave = onSaveFormaPagamento,
                        onShowToast = { message, type ->
                            toastNotification = ToastNotificationData(message, type)
                        },
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
                else ->
                    HomeScreen(
                        onNavigate = ::openDestination,
                        onLogout = {
                            coroutineScope.launch {
                                try {
                                    AuthSessionManager.logout()
                                } catch (exception: CancellationException) {
                                    throw exception
                                } catch (exception: Exception) {
                                    toastNotification =
                                        ToastNotificationData(
                                            "Não foi possível limpar a sessão neste dispositivo.",
                                            ToastNotificationType.ERROR,
                                        )
                                }
                            }
                        },
                    )
            }
            ToastNotification(
                notification = toastNotification,
                onDismiss = { toastNotification = null },
                modifier = Modifier.align(Alignment.TopEnd).padding(top = 16.dp, end = 16.dp, start = 16.dp),
            )
        }
    }
}

private enum class AppEntryScreen {
    LOADING,
    WELCOME,
    LOGIN,
    FIRST_ACCESS_LOGIN,
    FIRST_ACCESS_PASSWORD,
    HOME,
}
