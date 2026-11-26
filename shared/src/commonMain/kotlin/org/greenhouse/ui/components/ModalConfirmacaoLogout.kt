@file:Suppress("ktlint:standard:function-naming")

package org.greenhouse.ui.components

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
fun ModalConfirmacaoLogout(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val typography = AppTypography()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Sair da conta",
                color = TextPrimary,
                style = typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        },
        text = {
            Text(
                text = "Deseja realmente sair? Será necessário entrar novamente para acessar sua conta.",
                color = TextPrimary,
                style = typography.bodyMedium,
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = RedPrimary, contentColor = White),
            ) {
                Text("Sair", style = typography.bodyMedium)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar", color = GreenPrimary, style = typography.bodyMedium)
            }
        },
        containerColor = Color(0xFFFFFAE0),
    )
}
