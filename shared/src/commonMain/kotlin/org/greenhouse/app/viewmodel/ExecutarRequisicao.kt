package org.greenhouse.app.viewmodel

import androidx.compose.runtime.MutableState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.greenhouse.app.repository.mensagemDeErro

fun ViewModel.executarRequisicao(
    mensagem: MutableState<String>,
    mensagemErro: String,
    acao: suspend () -> Unit,
) {
    viewModelScope.launch {
        try {
            acao()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            mensagem.value = mensagemDeErro(e, mensagemErro)
        }
    }
}
