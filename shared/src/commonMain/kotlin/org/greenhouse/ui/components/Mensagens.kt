@file:Suppress("ktlint:standard:function-naming")

package org.greenhouse.ui.components

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState

@Composable
fun MostrarMensagem(
    snackbar: SnackbarHostState,
    mensagem: MutableState<String>,
) {
    LaunchedEffect(mensagem.value) {
        val texto = mensagem.value
        if (texto.isNotEmpty()) {
            try {
                snackbar.showSnackbar(texto)
            } finally {
                if (mensagem.value == texto) {
                    mensagem.value = ""
                }
            }
        }
    }
}
