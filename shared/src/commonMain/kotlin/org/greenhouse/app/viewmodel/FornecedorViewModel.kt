package org.greenhouse.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.greenhouse.app.model.Fornecedor
import org.greenhouse.app.model.FornecedorRequest
import org.greenhouse.app.repository.FornecedorRepositorioRemoto
import org.greenhouse.app.repository.mensagemDeErro
import org.greenhouse.ui.screens.fornecedor.model.FornecedorCadastroData

class FornecedorViewModel(
    private val repositorio: FornecedorRepositorioRemoto,
) : ViewModel() {
    private val _lista = MutableStateFlow<List<Fornecedor>>(emptyList())
    val lista = _lista.asStateFlow()

    var fornecedorEmEdicao by mutableStateOf<Fornecedor?>(null)
        private set
    var exibindoFormulario by mutableStateOf(false)
        private set
    var isCarregando by mutableStateOf(false)
        private set
    var isSalvando by mutableStateOf(false)
        private set
    var mensagemErro by mutableStateOf<String?>(null)
        private set
    var busca by mutableStateOf("")
        private set

    private var carregamento: Job? = null

    fun pesquisar(nome: String) {
        if (isSalvando) return
        busca = nome
        carregar(aguardarDigitacao = true)
    }

    fun carregar() {
        carregar(aguardarDigitacao = false)
    }

    private fun carregar(aguardarDigitacao: Boolean) {
        carregamento?.cancel()
        isCarregando = true
        mensagemErro = null
        val nome = busca.trim().ifEmpty { null }
        carregamento =
            viewModelScope.launch {
                try {
                    if (aguardarDigitacao) delay(300)
                    _lista.value = repositorio.listar(nome)
                } catch (exception: CancellationException) {
                    throw exception
                } catch (exception: Exception) {
                    val mensagem = mensagemDeErro(exception, "Não foi possível carregar os fornecedores")
                    if (isActive) mensagemErro = mensagem
                } finally {
                    if (isActive) isCarregando = false
                }
            }
    }

    fun novo() {
        fornecedorEmEdicao = null
        limparErro()
        exibindoFormulario = true
    }

    fun editar(id: Long) {
        executar("Não foi possível consultar o fornecedor") {
            fornecedorEmEdicao = repositorio.buscarPorId(id)
            exibindoFormulario = true
        }
    }

    fun voltarParaListagem() {
        if (isSalvando) return
        fecharFormulario()
    }

    private fun fecharFormulario() {
        fornecedorEmEdicao = null
        limparErro()
        exibindoFormulario = false
    }

    fun limparErro() {
        mensagemErro = null
    }

    fun gravar(
        dados: FornecedorCadastroData,
        onSuccess: (String) -> Unit,
    ) {
        val atual = fornecedorEmEdicao
        val request =
            FornecedorRequest(
                nome = dados.nome,
                cnpj = dados.cnpj,
                telefone = dados.telefone,
                email = dados.email,
                contato = dados.contato,
                ativo = dados.ativo,
                prazoMedioEntregaDias = dados.prazoMedioEntregaDias,
            )
        executar("Não foi possível gravar o fornecedor") {
            if (atual == null) {
                repositorio.salvar(request)
            } else {
                repositorio.atualizar(atual.id, request)
            }
            fecharFormulario()
            carregar()
            onSuccess(
                if (atual == null) {
                    "Fornecedor cadastrado com sucesso."
                } else {
                    "Fornecedor atualizado com sucesso."
                },
            )
        }
    }

    fun excluir(
        id: Long,
        onSuccess: (String) -> Unit,
    ) {
        executar("Não foi possível excluir o fornecedor") {
            repositorio.excluir(id)
            carregar()
            onSuccess("Fornecedor excluído com sucesso.")
        }
    }

    private fun executar(
        erroPadrao: String,
        acao: suspend () -> Unit,
    ) {
        if (isSalvando || isCarregando) return
        isSalvando = true
        limparErro()
        viewModelScope.launch {
            try {
                acao()
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                mensagemErro = mensagemDeErro(exception, erroPadrao)
            } finally {
                isSalvando = false
            }
        }
    }
}
