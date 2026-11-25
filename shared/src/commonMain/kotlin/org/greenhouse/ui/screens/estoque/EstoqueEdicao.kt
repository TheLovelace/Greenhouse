@file:Suppress("ktlint:standard:function-naming")

package org.greenhouse.ui.screens.estoque

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.greenhouse.ui.screens.estoque.model.EstoqueCadastroData
import org.greenhouse.ui.screens.estoque.model.EstoqueCadastroForm
import org.greenhouse.ui.screens.estoque.model.EstoqueItem

@Composable
fun EstoqueEdicao(
    estoque: EstoqueItem?,
    onBack: () -> Unit,
    onSave: (EstoqueCadastroData) -> Unit,
    existingProdutoIds: Set<Long> = emptySet(),
) {
    val initialForm =
        remember(estoque) {
            estoque?.let {
                EstoqueCadastroForm(
                    produtoId = it.produtoId.toString(),
                    quantidadeFisica = it.quantidadeFisica.toString(),
                    quantidadeMinima = it.quantidadeMinima.toString(),
                    localizacao = it.localizacao.orEmpty(),
                    bloqueado = it.bloqueado,
                )
            } ?: EstoqueCadastroForm()
        }

    EstoqueCadastro(
        onBack = onBack,
        onSave = onSave,
        initialForm = initialForm,
        title = if (estoque == null) "Cadastrar Estoque" else "Editar Estoque #${estoque.produtoId}",
        produtoIdReadOnly = estoque != null,
        quantidadeReservadaAtual = estoque?.quantidadeReservada,
        existingProdutoIds = existingProdutoIds,
    )
}
