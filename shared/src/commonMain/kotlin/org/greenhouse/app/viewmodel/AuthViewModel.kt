package org.greenhouse.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.greenhouse.app.repository.AuthRepositorioRemoto
import org.greenhouse.app.repository.AuthSessionManager
import org.greenhouse.app.repository.mensagemDeErro

class AuthViewModel(
    private val repositorio: AuthRepositorioRemoto,
) : ViewModel() {
    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun entrar(
        email: String,
        senha: String,
        lembrarDispositivo: Boolean,
    ) {
        if (isLoading) return
        isLoading = true
        errorMessage = null
        AuthSessionManager.clearMessage()
        viewModelScope.launch {
            try {
                val session = repositorio.entrar(email, senha, lembrarDispositivo)
                AuthSessionManager.startSession(session, lembrarDispositivo)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                errorMessage =
                    mensagemDeErro(
                        exception,
                        "Não foi possível entrar. Verifique seus dados e tente novamente.",
                    )
            } finally {
                isLoading = false
            }
        }
    }

    fun validarPrimeiroAcesso(
        login: String,
        onValid: () -> Unit,
    ) {
        if (isLoading) return
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            try {
                repositorio.validarPrimeiroAcesso(login)
                onValid()
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                errorMessage =
                    mensagemDeErro(
                        exception,
                        "Não foi possível validar o login. Confira os dados e tente novamente.",
                    )
            } finally {
                isLoading = false
            }
        }
    }

    fun definirSenhaPrimeiroAcesso(
        login: String,
        cpf: String,
        email: String,
        novaSenha: String,
        onSuccess: () -> Unit,
    ) {
        if (isLoading) return
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            try {
                repositorio.definirSenhaPrimeiroAcesso(login, cpf, email, novaSenha)
                onSuccess()
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                errorMessage =
                    mensagemDeErro(
                        exception,
                        "Não foi possível definir a senha. Confira os dados e tente novamente.",
                    )
            } finally {
                isLoading = false
            }
        }
    }

    fun clearError() {
        errorMessage = null
    }

    fun reportError(message: String) {
        errorMessage = message
    }
}
