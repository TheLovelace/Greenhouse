@file:Suppress("ktlint:standard:function-naming")

package org.greenhouse.ui.screens.compraFornecedor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import greenhouse.shared.generated.resources.Res
import greenhouse.shared.generated.resources.icon_pesquisa
import greenhouse.shared.generated.resources.icon_voltar
import org.greenhouse.app.model.CompraFornecedor
import org.greenhouse.app.viewmodel.CompraFornecedorViewModel
import org.greenhouse.ui.components.CadastroConfirmacao
import org.greenhouse.ui.components.CadastroListagem
import org.greenhouse.ui.components.CadastroListagemItem
import org.greenhouse.ui.components.ToastNotificationType
import org.greenhouse.ui.screens.compraFornecedor.model.CompraFornecedorCadastroData
import org.greenhouse.ui.screens.compraFornecedor.model.CompraFornecedorCadastroForm
import org.greenhouse.ui.screens.compraFornecedor.model.CompraItemForm
import org.greenhouse.ui.screens.compraFornecedor.model.StatusCompraCadastro
import org.greenhouse.ui.theme.AppTypography
import org.greenhouse.ui.theme.Background
import org.greenhouse.ui.theme.GreenPrimary
import org.greenhouse.ui.theme.GreenSecondary
import org.greenhouse.ui.theme.RedPrimary
import org.greenhouse.ui.theme.TextPrimary
import org.greenhouse.ui.theme.White
import org.jetbrains.compose.resources.painterResource
import kotlin.math.roundToLong

@Composable
fun CompraFornecedorGestaoScreen(
    viewModel: CompraFornecedorViewModel,
    onBack: () -> Unit,
    onAfterSave: (CompraFornecedorCadastroData) -> Unit,
    onShowToast: (String, ToastNotificationType) -> Unit,
) {
    val compras by viewModel.lista.collectAsState()
    val compra = viewModel.compraSelecionada
    LaunchedEffect(viewModel) {
        viewModel.carregar()
    }

    when {
        viewModel.exibindoFormulario -> {
            CompraFornecedorCadastro(
                onBack = viewModel::voltarDoFormulario,
                onSave = { dados ->
                    viewModel.gravar(dados) { mensagem ->
                        onShowToast(mensagem, ToastNotificationType.SUCCESS)
                        onAfterSave(dados)
                    }
                },
                initialForm = compra?.toCadastroForm() ?: CompraFornecedorCadastroForm(),
                recordId = compra?.id,
                isSaving = viewModel.isSalvando,
                errorMessage = viewModel.mensagemErro,
                onFormChanged = viewModel::limparErro,
            )
        }
        compra != null -> {
            CompraFornecedorDetalhes(
                compra = compra,
                isProcessando = viewModel.isSalvando,
                mensagemErro = viewModel.mensagemErro,
                onBack = viewModel::voltarParaListagem,
                onEdit = viewModel::editar,
                onAtualizarStatus = { status ->
                    viewModel.atualizarStatus(status) { atualizado ->
                        if (atualizado == StatusCompraCadastro.RECEBIDA) {
                            onShowToast("Recebimento confirmado.", ToastNotificationType.SUCCESS)
                        } else {
                            onShowToast("Compra cancelada com sucesso.", ToastNotificationType.DESTRUCTIVE)
                        }
                    }
                },
            )
        }
        else -> {
            CadastroListagem(
                titulo = "Compras de fornecedores",
                registros = compras.map(CompraFornecedor::toListagemItem),
                placeholderBusca = "Buscar por observação",
                mensagemListaVazia = "Nenhuma compra encontrada.",
                mensagemInativacao = { "Deseja excluir a compra #${it.id}?" },
                isCarregando = viewModel.isCarregando,
                isProcessando = viewModel.isSalvando || viewModel.isCarregando,
                mensagemErro = viewModel.mensagemErro,
                onRetry = viewModel::carregar,
                onBack = { if (!viewModel.isSalvando) onBack() },
                onCreate = viewModel::novo,
                onEdit = viewModel::consultar,
                onDeactivate = { id ->
                    viewModel.excluir(id) { mensagem ->
                        onShowToast(mensagem, ToastNotificationType.DESTRUCTIVE)
                    }
                },
                buscaRemota = viewModel.busca,
                onBuscaRemota = viewModel::pesquisar,
                filtrosStatus = mapOf("Pedidas" to "Pedida", "Recebidas" to "Recebida", "Canceladas" to "Cancelada"),
                rotuloAcaoPrincipal = "Consultar",
                iconeAcaoPrincipal = Res.drawable.icon_pesquisa,
                rotuloAcaoDestrutiva = "Excluir",
            )
        }
    }
}

@Composable
private fun CompraFornecedorDetalhes(
    compra: CompraFornecedor,
    isProcessando: Boolean,
    mensagemErro: String?,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onAtualizarStatus: (StatusCompraCadastro) -> Unit,
) {
    val typography = AppTypography()
    var novoStatus by remember(compra.id) { mutableStateOf<StatusCompraCadastro?>(null) }
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.fillMaxWidth().height(52.dp), contentAlignment = Alignment.Center) {
            Text("Compras", color = TextPrimary, style = typography.titleMedium)
            IconButton(
                onClick = onBack,
                enabled = !isProcessando,
                modifier = Modifier.align(Alignment.CenterStart).size(48.dp),
            ) {
                Image(
                    painter = painterResource(Res.drawable.icon_voltar),
                    contentDescription = "Voltar",
                    modifier = Modifier.size(32.dp),
                )
            }
        }
        Column(
            modifier = Modifier.widthIn(max = 760.dp).fillMaxWidth().padding(vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("Compra #${compra.id}", color = TextPrimary, style = typography.headlineLarge)
            val detalhes =
                listOf(
                    "Status: ${compra.status.label()}",
                    "Fornecedor: #${compra.fornecedorId}",
                    "Funcionário: #${compra.funcionarioId}",
                    "Forma de pagamento: #${compra.formaPagamentoId}",
                ) + compra.detalhes()
            detalhes.forEach { Text(it, color = TextPrimary, style = typography.bodyMedium) }

            Text("Itens", color = TextPrimary, style = typography.titleMedium)
            compra.itens.forEach { item ->
                Text(
                    "Produto #${item.produtoId} • ${item.quantidade} × R$ ${item.valorUnitario.toValorFormulario()}",
                    color = TextPrimary,
                    style = typography.bodyMedium,
                )
            }

            mensagemErro?.let { Text(it, color = RedPrimary, style = typography.bodyMedium) }
            if (compra.status == StatusCompraCadastro.PEDIDA) {
                Button(
                    onClick = { novoStatus = StatusCompraCadastro.RECEBIDA },
                    enabled = !isProcessando,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary, contentColor = White),
                ) {
                    Text("Confirmar recebimento", style = typography.labelLarge)
                }
                OutlinedButton(
                    onClick = onEdit,
                    enabled = !isProcessando,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, GreenPrimary),
                ) {
                    Text("Editar", color = GreenPrimary, style = typography.labelLarge)
                }
                OutlinedButton(
                    onClick = { novoStatus = StatusCompraCadastro.CANCELADA },
                    enabled = !isProcessando,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, RedPrimary),
                ) {
                    Text("Cancelar compra", color = RedPrimary, style = typography.labelLarge)
                }
            }
        }
    }

    novoStatus?.let { status ->
        val recebendo = status == StatusCompraCadastro.RECEBIDA
        CadastroConfirmacao(
            titulo = if (recebendo) "Confirmar recebimento?" else "Cancelar compra?",
            mensagem =
                if (recebendo) {
                    "Deseja receber a compra #${compra.id} e dar entrada nos itens no estoque?"
                } else {
                    "Deseja cancelar a compra #${compra.id}?"
                },
            rotuloConfirmar = if (recebendo) "Receber" else "Cancelar compra",
            onConfirm = {
                novoStatus = null
                onAtualizarStatus(status)
            },
            onDismiss = { novoStatus = null },
            isProcessando = isProcessando,
            destrutiva = !recebendo,
            rotuloCancelar = "Voltar",
        )
    }
}

private fun CompraFornecedor.toListagemItem() =
    CadastroListagemItem(
        id = id,
        titulo = "Fornecedor #$fornecedorId",
        detalhes = detalhes(),
        ativo = status == StatusCompraCadastro.PEDIDA,
        status = status.label(),
        corStatus =
            when (status) {
                StatusCompraCadastro.PEDIDA -> GreenPrimary
                StatusCompraCadastro.RECEBIDA -> GreenSecondary
                StatusCompraCadastro.CANCELADA -> RedPrimary
            },
    )

private fun CompraFornecedor.detalhes(): List<String> {
    val frete = valorFrete?.let { "R$ ${it.toValorFormulario()}" } ?: "não informado"
    val indicacaoFrete = if (freteIncluso) " (incluso nos itens)" else ""
    return listOfNotNull(
        "Data da compra: ${dataCompra.toDataFormulario()}",
        dataEntregaPrevista?.let { "Previsão de entrega: ${it.toDataFormulario()}" },
        "Valor total: R$ ${valorTotal.toValorFormulario()}",
        "Frete: $frete$indicacaoFrete",
        observacao?.takeIf(String::isNotBlank)?.let { "Observação: $it" },
    )
}

private fun CompraFornecedor.toCadastroForm() =
    CompraFornecedorCadastroForm(
        funcionarioId = funcionarioId.toString(),
        fornecedorId = fornecedorId.toString(),
        formaPagamentoId = formaPagamentoId.toString(),
        dataCompra = dataCompra.toDataFormulario(),
        status = status,
        valorFrete = valorFrete?.toValorFormulario().orEmpty(),
        freteIncluso = freteIncluso,
        dataEntregaPrevista = dataEntregaPrevista?.toDataFormulario().orEmpty(),
        observacao = observacao.orEmpty(),
        itens = itens.map { CompraItemForm(it.produtoId.toString(), it.quantidade.toString(), it.valorUnitario.toValorFormulario()) },
    )

private fun String.toDataFormulario(): String = substringBefore('T').split('-').asReversed().joinToString("/")

private fun Double.toValorFormulario(): String {
    val centavos = (this * 100).roundToLong()
    return "${centavos / 100},${(centavos % 100).toString().padStart(2, '0')}"
}
