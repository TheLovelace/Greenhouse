@file:Suppress("ktlint:standard:function-naming")

package org.greenhouse.ui.screens.estoque

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import org.greenhouse.app.viewmodel.EstoqueViewModel

@Composable
fun EstoqueEdicao(
    viewModel: EstoqueViewModel,
    onBack: () -> Unit,
    onSuccess: (String) -> Unit,
    padding: PaddingValues = PaddingValues(),
    embedded: Boolean = false,
) {
    val produtoId = viewModel.produtoId.value
    EstoqueCadastro(
        viewModel = viewModel,
        onBack = onBack,
        onSuccess = onSuccess,
        title = if (viewModel.editando.value) "Editar Estoque #$produtoId" else "Cadastrar Estoque",
        padding = padding,
        embedded = embedded,
    )
}
