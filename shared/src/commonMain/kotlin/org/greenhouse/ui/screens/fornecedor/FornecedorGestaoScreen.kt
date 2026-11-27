@file:Suppress("ktlint:standard:function-naming")

package org.greenhouse.ui.screens.fornecedor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.greenhouse.app.model.Fornecedor
import org.greenhouse.app.viewmodel.FornecedorViewModel
import org.greenhouse.ui.components.CadastroConfirmacao
import org.greenhouse.ui.components.CadastroListagem
import org.greenhouse.ui.components.CadastroListagemItem
import org.greenhouse.ui.components.ToastNotificationType
import org.greenhouse.ui.screens.fornecedor.model.FornecedorCadastroData
import org.greenhouse.ui.screens.fornecedor.model.FornecedorCadastroForm
import org.greenhouse.ui.screens.fornecedor.validation.formatarCnpj

@Composable
fun FornecedorGestaoScreen(
    viewModel: FornecedorViewModel,
    onBack: () -> Unit,
    onAfterSave: (FornecedorCadastroData) -> Unit,
    onShowToast: (String, ToastNotificationType) -> Unit,
) {
    val fornecedores by viewModel.lista.collectAsState()
    LaunchedEffect(viewModel) {
        viewModel.carregar()
    }

    if (viewModel.exibindoFormulario) {
        val fornecedor = viewModel.fornecedorEmEdicao
        var dadosParaInativar by remember { mutableStateOf<FornecedorCadastroData?>(null) }

        fun gravar(dados: FornecedorCadastroData) {
            viewModel.gravar(dados) { mensagem ->
                onShowToast(mensagem, ToastNotificationType.SUCCESS)
                onAfterSave(dados)
            }
        }

        FornecedorCadastro(
            onBack = viewModel::voltarParaListagem,
            onSave = { dados ->
                if (fornecedor?.ativo == true && !dados.ativo) {
                    dadosParaInativar = dados
                } else {
                    gravar(dados)
                }
            },
            initialForm = fornecedor?.toCadastroForm() ?: FornecedorCadastroForm(),
            recordId = fornecedor?.id,
            isSaving = viewModel.isSalvando,
            errorMessage = viewModel.mensagemErro,
            onFormChanged = viewModel::limparErro,
        )
        dadosParaInativar?.let { dados ->
            CadastroConfirmacao(
                titulo = "Inativar fornecedor?",
                mensagem = "Deseja inativar ${dados.nome}? Ele ficará indisponível para novas compras.",
                rotuloConfirmar = "Inativar",
                onConfirm = {
                    dadosParaInativar = null
                    gravar(dados)
                },
                onDismiss = { dadosParaInativar = null },
            )
        }
    } else {
        CadastroListagem(
            titulo = "Fornecedores cadastrados",
            registros =
                fornecedores.map { fornecedor ->
                    CadastroListagemItem(
                        id = fornecedor.id,
                        titulo = fornecedor.nome,
                        detalhes =
                            listOfNotNull(
                                fornecedor.cnpj?.let { "CNPJ: ${formatarCnpj(it)}" },
                                fornecedor.telefone?.let { "Telefone: $it" },
                                fornecedor.email?.let { "E-mail: $it" },
                                fornecedor.contato?.let { "Contato: $it" },
                                fornecedor.prazoMedioEntregaDias?.let { "Prazo médio de entrega: $it dias" },
                            ),
                        ativo = fornecedor.ativo,
                    )
                },
            placeholderBusca = "Buscar por nome",
            mensagemListaVazia = "Nenhum fornecedor encontrado.",
            mensagemInativacao = { "Deseja excluir ${it.titulo}?" },
            isCarregando = viewModel.isCarregando,
            isProcessando = viewModel.isSalvando || viewModel.isCarregando,
            mensagemErro = viewModel.mensagemErro,
            onRetry = viewModel::carregar,
            onBack = { if (!viewModel.isSalvando) onBack() },
            onCreate = viewModel::novo,
            onEdit = viewModel::editar,
            onDeactivate = { id ->
                viewModel.excluir(id) { mensagem ->
                    onShowToast(mensagem, ToastNotificationType.DESTRUCTIVE)
                }
            },
            buscaRemota = viewModel.busca,
            onBuscaRemota = viewModel::pesquisar,
            rotuloAcaoDestrutiva = "Excluir",
            mostrarAcaoDestrutiva = { true },
        )
    }
}

private fun Fornecedor.toCadastroForm() =
    FornecedorCadastroForm(
        nome = nome,
        cnpj = cnpj?.let(::formatarCnpj).orEmpty(),
        telefone = telefone.orEmpty(),
        email = email.orEmpty(),
        contato = contato.orEmpty(),
        ativo = ativo,
        prazoMedioEntregaDias = prazoMedioEntregaDias?.toString().orEmpty(),
    )
