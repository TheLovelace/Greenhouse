@file:Suppress("ktlint:standard:function-naming")

package org.greenhouse.ui.screens.formaPagamento

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.greenhouse.app.model.FormaPagamento
import org.greenhouse.app.viewmodel.FormaPagamentoViewModel
import org.greenhouse.ui.components.CadastroListagem
import org.greenhouse.ui.components.CadastroListagemItem
import org.greenhouse.ui.components.ToastNotificationType
import org.greenhouse.ui.screens.formaPagamento.model.FormaPagamentoCadastroData

@Composable
fun FormaPagamentoGestaoScreen(
    viewModel: FormaPagamentoViewModel,
    onBack: () -> Unit,
    onAfterSave: (FormaPagamentoCadastroData) -> Unit,
    onShowToast: (String, ToastNotificationType) -> Unit,
) {
    val formasPagamento by viewModel.lista.collectAsState()
    val exibindoFormulario = viewModel.exibindoFormulario.value

    LaunchedEffect(exibindoFormulario) {
        if (!exibindoFormulario) viewModel.carregar()
    }

    if (exibindoFormulario) {
        val formaPagamento = viewModel.formaPagamentoEmEdicao.value
        FormaPagamentoCadastro(
            onBack = viewModel::voltarParaListagem,
            onSave = { dados ->
                viewModel.gravar(dados) { mensagem ->
                    onAfterSave(dados)
                    onShowToast(mensagem, ToastNotificationType.SUCCESS)
                }
            },
            initialData = formaPagamento?.toCadastroData(),
            recordId = formaPagamento?.id,
            isSaving = viewModel.isSalvando.value,
            errorMessage = viewModel.mensagemErro.value,
            onFormChanged = viewModel::limparErro,
        )
    } else {
        CadastroListagem(
            titulo = "Formas de pagamento cadastradas",
            registros =
                formasPagamento.map { formaPagamento ->
                    CadastroListagemItem(
                        id = formaPagamento.id,
                        titulo = formaPagamento.nome,
                        detalhes =
                            listOfNotNull(
                                "Limite de parcelas: ${formaPagamento.limiteParcelas}",
                                formaPagamento.descricao?.takeIf(String::isNotBlank)?.let { "Descrição: $it" },
                                formaPagamento.prazoEstornoDias?.let { "Prazo de estorno: $it dias" },
                            ),
                        ativo = formaPagamento.ativo,
                    )
                },
            placeholderBusca = "Buscar por nome ou descrição",
            mensagemListaVazia = "Nenhuma forma de pagamento cadastrada.",
            mensagemInativacao = { "Deseja inativar ${it.titulo}? Ela não ficará disponível para novos pedidos." },
            isCarregando = viewModel.isCarregando.value,
            isProcessando = viewModel.isSalvando.value,
            mensagemErro = viewModel.mensagemErro.value,
            onRetry = viewModel::carregar,
            onBack = onBack,
            onCreate = viewModel::novo,
            onEdit = { id -> formasPagamento.firstOrNull { it.id == id }?.let(viewModel::editar) },
            onDeactivate = { id ->
                formasPagamento.firstOrNull { it.id == id }?.let { formaPagamento ->
                    viewModel.excluir(formaPagamento) { mensagem ->
                        onShowToast(mensagem, ToastNotificationType.DESTRUCTIVE)
                    }
                }
            },
        )
    }
}

private fun FormaPagamento.toCadastroData() =
    FormaPagamentoCadastroData(
        nome = nome,
        ativo = ativo,
        descricao = descricao,
        limiteParcelas = limiteParcelas,
        prazoEstornoDias = prazoEstornoDias,
    )
