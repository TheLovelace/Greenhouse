@file:Suppress("ktlint:standard:function-naming")

package org.greenhouse.ui.screens.funcionario

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.greenhouse.app.model.Funcionario
import org.greenhouse.app.viewmodel.FuncionarioViewModel
import org.greenhouse.ui.components.CadastroListagem
import org.greenhouse.ui.components.CadastroListagemItem
import org.greenhouse.ui.components.ToastNotificationType
import org.greenhouse.ui.screens.funcionario.model.FuncionarioCadastroData

@Composable
fun FuncionarioGestaoScreen(
    viewModel: FuncionarioViewModel,
    onBack: () -> Unit,
    onAfterSave: (FuncionarioCadastroData) -> Unit,
    onShowToast: (String, ToastNotificationType) -> Unit,
    embedded: Boolean = false,
) {
    val funcionarios by viewModel.lista.collectAsState()
    val exibindoFormulario = viewModel.exibindoFormulario.value

    LaunchedEffect(exibindoFormulario) {
        if (!exibindoFormulario) viewModel.carregar()
    }

    if (exibindoFormulario) {
        val funcionario = viewModel.funcionarioEmEdicao.value
        FuncionarioCadastro(
            onBack = viewModel::voltarParaListagem,
            onSave = { dados ->
                viewModel.gravar(dados) { mensagem ->
                    onAfterSave(dados)
                    onShowToast(mensagem, ToastNotificationType.SUCCESS)
                }
            },
            initialData = funcionario?.toCadastroData(),
            recordId = funcionario?.id,
            isSaving = viewModel.isSalvando.value,
            errorMessage = viewModel.mensagemErro.value,
            onFormChanged = viewModel::limparErro,
            embedded = embedded,
        )
    } else {
        CadastroListagem(
            titulo = "Funcionários cadastrados",
            registros =
                funcionarios.map { funcionario ->
                    CadastroListagemItem(
                        id = funcionario.id,
                        titulo = funcionario.nome,
                        detalhes =
                            listOfNotNull(
                                "CPF: ${funcionario.cpf}",
                                funcionario.email?.let { "E-mail: $it" },
                                funcionario.cargo?.takeIf(String::isNotBlank)?.let { "Cargo: $it" },
                                funcionario.telefone?.takeIf(String::isNotBlank)?.let { "Telefone: $it" },
                                funcionario.dataDeNascimento?.let { "Nascimento: ${it.toDataBrasileira()}" },
                            ),
                        ativo = funcionario.ativo,
                    )
                },
            placeholderBusca = "Buscar por nome, CPF ou e-mail",
            mensagemListaVazia = "Nenhum funcionário cadastrado.",
            mensagemInativacao = { "Deseja inativar ${it.titulo}? O acesso ao sistema também será bloqueado." },
            isCarregando = viewModel.isCarregando.value,
            isProcessando = viewModel.isSalvando.value,
            mensagemErro = viewModel.mensagemErro.value,
            onRetry = viewModel::carregar,
            onBack = onBack,
            onCreate = viewModel::novo,
            onEdit = { id -> funcionarios.firstOrNull { it.id == id }?.let(viewModel::editar) },
            onDeactivate = { id ->
                funcionarios.firstOrNull { it.id == id }?.let { funcionario ->
                    viewModel.desativar(funcionario) { mensagem ->
                        onShowToast(mensagem, ToastNotificationType.DESTRUCTIVE)
                    }
                }
            },
            embedded = embedded,
        )
    }
}

private fun Funcionario.toCadastroData() =
    FuncionarioCadastroData(
        nome = nome,
        cpf = cpf,
        telefone = telefone,
        email = email.orEmpty(),
        cargo = cargo,
        ativo = ativo,
        dataDeNascimento = dataDeNascimento,
    )

private fun String.toDataBrasileira(): String {
    val partes = substringBefore('T').split('-')
    return if (partes.size == 3) "${partes[2]}/${partes[1]}/${partes[0]}" else this
}
