@file:Suppress("ktlint:standard:function-naming")

package org.greenhouse.ui.screens.especie

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import org.greenhouse.app.viewmodel.EspecieViewModel

@Composable
fun EspecieEdicao(
    viewModel: EspecieViewModel,
    onBack: () -> Unit,
    onSuccess: (String) -> Unit,
    padding: PaddingValues = PaddingValues(),
    embedded: Boolean = false,
) {
    val especieId = viewModel.id.value
    EspecieCadastro(
        viewModel = viewModel,
        onBack = onBack,
        onSuccess = onSuccess,
        title = if (especieId.isEmpty()) "Cadastrar Espécie" else "Editar Espécie #$especieId",
        padding = padding,
        embedded = embedded,
    )
}
