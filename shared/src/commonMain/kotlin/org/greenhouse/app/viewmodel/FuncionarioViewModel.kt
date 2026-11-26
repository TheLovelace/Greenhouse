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
import org.greenhouse.app.model.Funcionario
import org.greenhouse.app.model.FuncionarioRequest
import org.greenhouse.app.repository.FuncionarioRepositorioRemoto
import org.greenhouse.app.repository.mensagemDeErro
import org.greenhouse.ui.screens.funcionario.model.FuncionarioCadastroData

class FuncionarioViewModel(
    private val repositorio: FuncionarioRepositorioRemoto,
) : ViewModel() {
    private val _lista = MutableStateFlow<List<Funcionario>>(emptyList())
    val lista: StateFlow<List<Funcionario>> = _lista.asStateFlow()

    var funcionarioEmEdicao: MutableState<Funcionario?> = mutableStateOf(null)
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
                mensagemErro.value = mensagemDeErro(exception, "Não foi possível carregar os funcionários")
            } finally {
                isCarregando.value = false
            }
        }
    }

    fun novo() {
        funcionarioEmEdicao.value = null
        mensagemErro.value = null
        exibindoFormulario.value = true
    }

    fun editar(funcionario: Funcionario) {
        funcionarioEmEdicao.value = funcionario
        mensagemErro.value = null
        exibindoFormulario.value = true
    }

    fun voltarParaListagem() {
        funcionarioEmEdicao.value = null
        mensagemErro.value = null
        exibindoFormulario.value = false
    }

    fun limparErro() {
        mensagemErro.value = null
    }

    fun gravar(
        dados: FuncionarioCadastroData,
        onSuccess: (String) -> Unit,
    ) {
        if (isSalvando.value) return
        val atual = funcionarioEmEdicao.value
        val request =
            FuncionarioRequest(
                nome = dados.nome,
                cpf = dados.cpf,
                telefone = dados.telefone,
                email = dados.email,
                cargo = dados.cargo,
                ativo = dados.ativo,
                dataDeNascimento = dados.dataDeNascimento,
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
                funcionarioEmEdicao.value = null
                exibindoFormulario.value = false
                onSuccess(if (atual == null) "Funcionário cadastrado com sucesso." else "Funcionário atualizado com sucesso.")
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                mensagemErro.value = mensagemDeErro(exception, "Não foi possível gravar o funcionário")
            } finally {
                isSalvando.value = false
            }
        }
    }

    fun desativar(
        funcionario: Funcionario,
        onSuccess: (String) -> Unit,
    ) {
        if (isSalvando.value) return
        viewModelScope.launch {
            isSalvando.value = true
            mensagemErro.value = null
            try {
                repositorio.desativar(funcionario.id)
                _lista.value = _lista.value.map { if (it.id == funcionario.id) it.copy(ativo = false) else it }
                onSuccess("Funcionário inativado; o acesso ao sistema foi bloqueado.")
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                mensagemErro.value = mensagemDeErro(exception, "Não foi possível inativar o funcionário")
            } finally {
                isSalvando.value = false
            }
        }
    }
}
