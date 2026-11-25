@file:Suppress("ktlint:standard:function-naming")

package org.greenhouse.ui.screens.desconto

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import org.greenhouse.app.viewmodel.DescontoViewModel

@Composable
fun DescontoEdicao(
    viewModel: DescontoViewModel,
    onBack: () -> Unit,
    onSuccess: (String) -> Unit,
    padding: PaddingValues = PaddingValues(),
    embedded: Boolean = false,
) {
    val descontoId = viewModel.id.value
    DescontoCadastro(
        viewModel = viewModel,
        onBack = onBack,
        onSuccess = onSuccess,
        title = if (descontoId.isEmpty()) "Cadastrar Desconto" else "Editar Desconto #$descontoId",
        padding = padding,
        embedded = embedded,
    )
}
