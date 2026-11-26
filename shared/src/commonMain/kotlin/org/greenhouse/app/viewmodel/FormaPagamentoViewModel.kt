package org.greenhouse.app.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.greenhouse.app.model.FormaPagamento
import org.greenhouse.app.model.FormaPagamentoRequest
import org.greenhouse.app.repository.FormaPagamentoRepositorioRemoto
import org.greenhouse.app.repository.mensagemDeErro
import org.greenhouse.ui.screens.formaPagamento.model.FormaPagamentoCadastroData

class FormaPagamentoViewModel(
    private val repositorio: FormaPagamentoRepositorioRemoto,
) : ViewModel() {
    private val _lista = MutableStateFlow<List<FormaPagamento>>(emptyList())
    val lista: StateFlow<List<FormaPagamento>> = _lista.asStateFlow()

    var formaPagamentoEmEdicao: MutableState<FormaPagamento?> = mutableStateOf(null)
    var exibindoFormulario: MutableState<Boolean> = mutableStateOf(false)
    var isCarregando: MutableState<Boolean> = mutableStateOf(false)
    var isSalvando: MutableState<Boolean> = mutableStateOf(false)
    var mensagemErro: MutableState<String?> = mutableStateOf(null)

    fun carregar() {
        if (isCarregando.value) return
        viewModelScope.launch {
            isCarregando.value = true
            mensagemErro.value = null
            try {
                _lista.value = repositorio.listar()
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                mensagemErro.value = mensagemDeErro(exception, "Não foi possível carregar as formas de pagamento")
            } finally {
                isCarregando.value = false
            }
        }
    }

    fun novo() {
        formaPagamentoEmEdicao.value = null
        mensagemErro.value = null
        exibindoFormulario.value = true
    }

    fun editar(formaPagamento: FormaPagamento) {
        formaPagamentoEmEdicao.value = formaPagamento
        mensagemErro.value = null
        exibindoFormulario.value = true
    }

    fun voltarParaListagem() {
        formaPagamentoEmEdicao.value = null
        mensagemErro.value = null
        exibindoFormulario.value = false
    }

    fun limparErro() {
        mensagemErro.value = null
    }

    fun gravar(
        dados: FormaPagamentoCadastroData,
        onSuccess: (String) -> Unit,
    ) {
        if (isSalvando.value) return
        val atual = formaPagamentoEmEdicao.value
        val request =
            FormaPagamentoRequest(
                nome = dados.nome,
                ativo = dados.ativo,
                descricao = dados.descricao,
                limiteParcelas = dados.limiteParcelas,
                prazoEstornoDias = dados.prazoEstornoDias,
            )

        viewModelScope.launch {
            isSalvando.value = true
            mensagemErro.value = null
            try {
                val salvo =
                    if (atual == null) {
                        repositorio.salvar(request)
                    } else {
                        repositorio.atualizar(atual.id, request)
                    }
                _lista.value =
                    if (atual == null) {
                        _lista.value + salvo
                    } else {
                        _lista.value.map { if (it.id == salvo.id) salvo else it }
                    }
                formaPagamentoEmEdicao.value = null
                exibindoFormulario.value = false
                onSuccess(
                    if (atual == null) {
                        "Forma de pagamento cadastrada com sucesso."
                    } else {
                        "Forma de pagamento atualizada com sucesso."
                    },
                )
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                mensagemErro.value = mensagemDeErro(exception, "Não foi possível gravar a forma de pagamento")
            } finally {
                isSalvando.value = false
            }
        }
    }

    fun excluir(
        formaPagamento: FormaPagamento,
        onSuccess: (String) -> Unit,
    ) {
        if (isSalvando.value) return
        viewModelScope.launch {
            isSalvando.value = true
            mensagemErro.value = null
            try {
                repositorio.excluir(formaPagamento.id)
                _lista.value =
                    _lista.value.map {
                        if (it.id == formaPagamento.id) it.copy(ativo = false) else it
                    }
                onSuccess("Forma de pagamento inativada com sucesso.")
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                mensagemErro.value = mensagemDeErro(exception, "Não foi possível inativar a forma de pagamento")
            } finally {
                isSalvando.value = false
            }
        }
    }
}
