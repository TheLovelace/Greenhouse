@file:Suppress("ktlint:standard:function-naming")

package org.greenhouse.ui.screens.desconto

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import org.greenhouse.ui.theme.AppTypography
import org.greenhouse.ui.theme.GreenPrimary
import org.greenhouse.ui.theme.RedPrimary
import org.greenhouse.ui.theme.TextPrimary
import org.greenhouse.ui.theme.White

@Composable
fun ModalConfirmacaoDesconto(
    titulo: String,
    mensagem: String,
    textoConfirmacao: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(titulo, color = TextPrimary, style = AppTypography().titleMedium, fontWeight = FontWeight.SemiBold)
        },
        text = {
            Text(mensagem, color = TextPrimary, style = AppTypography().bodyMedium)
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = RedPrimary, contentColor = White),
            ) {
                Text(textoConfirmacao, style = AppTypography().bodyMedium)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar", color = GreenPrimary, style = AppTypography().bodyMedium)
            }
        },
        containerColor = Color(0xFFFFFAE0),
    )
}
