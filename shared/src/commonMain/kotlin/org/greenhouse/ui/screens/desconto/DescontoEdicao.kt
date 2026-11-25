@file:Suppress("ktlint:standard:function-naming")

package org.greenhouse.ui.screens.desconto

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.greenhouse.ui.screens.desconto.model.DescontoCadastroData
import org.greenhouse.ui.screens.desconto.model.DescontoCadastroForm
import org.greenhouse.ui.screens.desconto.model.DescontoItem

@Composable
fun DescontoEdicao(
    desconto: DescontoItem?,
    onBack: () -> Unit,
    onSave: (DescontoCadastroData) -> Unit,
) {
    val initialForm = remember(desconto) { desconto?.toCadastroForm() ?: DescontoCadastroForm() }
    DescontoCadastro(
        onBack = onBack,
        onSave = onSave,
        initialForm = initialForm,
        title = if (desconto == null) "Cadastrar Desconto" else "Editar Desconto #${desconto.id}",
    )
}

private fun DescontoItem.toCadastroForm(): DescontoCadastroForm =
    DescontoCadastroForm(
        titulo = titulo,
        descricao = descricao.orEmpty(),
        tipo = tipo,
        valor = valor.replace('.', ','),
        dataInicio = dataInicio.toDataBrasileira(),
        dataFim = dataFim.toDataBrasileira(),
        ativo = ativo,
        quantidadeMinimaItens = quantidadeMinimaItens.toString(),
    )

private fun String?.toDataBrasileira(): String {
    if (this == null) return ""
    val data = substringBefore('T')
    val partes = data.split('-')
    if (partes.size != 3) return ""
    return "${partes[2]}/${partes[1]}/${partes[0]}"
}
