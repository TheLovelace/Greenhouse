@file:Suppress("ktlint:standard:function-naming")

package org.greenhouse.ui.screens.estoque

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import org.greenhouse.ui.screens.estoque.model.EstoqueItem
import org.greenhouse.ui.theme.AppTypography
import org.greenhouse.ui.theme.GreenPrimary
import org.greenhouse.ui.theme.RedPrimary
import org.greenhouse.ui.theme.TextPrimary
import org.greenhouse.ui.theme.White

@Composable
fun ModalConfirmacaoEstoque(
    estoque: EstoqueItem,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val podeExcluir = estoque.quantidadeReservada == 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (podeExcluir) "Excluir estoque" else "Exclusão não permitida",
                color = TextPrimary,
                style = AppTypography().titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        },
        text = {
            Text(
                text =
                    if (podeExcluir) {
                        "Deseja realmente excluir o estoque do produto #${estoque.produtoId}?"
                    } else {
                        "Este estoque possui ${estoque.quantidadeReservada} item(ns) reservado(s) e não pode ser excluído."
                    },
                color = TextPrimary,
                style = AppTypography().bodyMedium,
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = if (podeExcluir) RedPrimary else GreenPrimary,
                        contentColor = White,
                    ),
            ) {
                Text(if (podeExcluir) "Excluir" else "Entendi", style = AppTypography().bodyMedium)
            }
        },
        dismissButton = {
            if (podeExcluir) {
                OutlinedButton(onClick = onDismiss) {
                    Text("Cancelar", color = GreenPrimary, style = AppTypography().bodyMedium)
                }
            }
        },
        containerColor = Color(0xFFFFFAE0),
    )
}
