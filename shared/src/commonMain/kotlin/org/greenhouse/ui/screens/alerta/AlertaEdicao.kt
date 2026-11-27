@file:Suppress("ktlint:standard:function-naming")

package org.greenhouse.ui.screens.alerta

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import org.greenhouse.app.viewmodel.AlertaViewModel

@Composable
fun AlertaEdicao(
    viewModel: AlertaViewModel,
    onBack: () -> Unit,
    onSuccess: (String) -> Unit,
    padding: PaddingValues = PaddingValues(),
    embedded: Boolean = false,
) {
    val alertaId = viewModel.id.value
    AlertaCadastro(
        viewModel = viewModel,
        onBack = onBack,
        onSuccess = onSuccess,
        title = if (alertaId.isEmpty()) "Cadastrar Alerta IoT" else "Editar Alerta IoT #$alertaId",
        padding = padding,
        embedded = embedded,
    )
}
