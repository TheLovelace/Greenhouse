package org.greenhouse.app.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.navigation3.runtime.NavBackStack
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.greenhouse.app.model.Estoque
import org.greenhouse.app.model.EstoqueCreateRequest
import org.greenhouse.app.model.EstoqueUpdateRequest
import org.greenhouse.app.repository.EstoqueRepositorioRemoto
import org.greenhouse.ui.navigation.EstoqueRotas
import org.greenhouse.ui.screens.estoque.model.EstoqueCadastroForm
import org.greenhouse.ui.screens.estoque.model.EstoqueItem
import org.greenhouse.ui.screens.estoque.validation.toEstoqueCadastroDataOrNull
import org.greenhouse.ui.screens.estoque.validation.validarEstoqueCadastro

class EstoqueViewModel(
    private val repositorio: EstoqueRepositorioRemoto,
) : ViewModel() {
    private val _lista = MutableStateFlow<List<EstoqueItem>>(emptyList())
    val lista: StateFlow<List<EstoqueItem>> = _lista.asStateFlow()

    var produtoId: MutableState<String> = mutableStateOf("")
    var quantidadeFisica: MutableState<String> = mutableStateOf("0")
    var quantidadeMinima: MutableState<String> = mutableStateOf("0")
    var localizacao: MutableState<String> = mutableStateOf("")
    var bloqueado: MutableState<Boolean> = mutableStateOf(false)
    var editando: MutableState<Boolean> = mutableStateOf(false)
    var quantidadeReservadaAtual: MutableState<Int?> = mutableStateOf(null)
    var erros: MutableState<Map<String, String>> = mutableStateOf(emptyMap())
    var mensagem: MutableState<String> = mutableStateOf("")

    val backStack = NavBackStack<EstoqueRotas>(EstoqueRotas.listagem)

    fun carregar() {
        executarRequisicao(mensagem, "Não foi possível carregar o estoque") {
            atualizarLista()
        }
    }

    fun gravar(onSuccess: (String) -> Unit = {}) {
        val form = formulario()
        val errosValidacao = validarEstoqueCadastro(form, quantidadeReservadaAtual.value).toMutableMap()
        val idProduto = form.produtoId.trim().toLongOrNull()
        if (!editando.value && idProduto != null && _lista.value.any { it.produtoId == idProduto }) {
            errosValidacao["produtoId"] = "Já existe um registro de estoque para este produto"
        }
        erros.value = errosValidacao
        val dados = form.toEstoqueCadastroDataOrNull()
        if (errosValidacao.isNotEmpty() || dados == null) {
            mensagem.value = "Corrija os campos destacados"
            return
        }

        val request =
            EstoqueUpdateRequest(
                quantidadeFisica = dados.quantidadeFisica,
                quantidadeMinima = dados.quantidadeMinima,
                localizacao = dados.localizacao,
                bloqueado = dados.bloqueado,
            )
        val estavaEditando = editando.value

        executarRequisicao(mensagem, "Não foi possível gravar o estoque") {
            if (estavaEditando) {
                repositorio.atualizar(dados.produtoId, request)
            } else {
                repositorio.salvar(
                    EstoqueCreateRequest(
                        produtoId = dados.produtoId,
                        quantidadeFisica = dados.quantidadeFisica,
                        quantidadeMinima = dados.quantidadeMinima,
                        localizacao = dados.localizacao,
                        bloqueado = dados.bloqueado,
                    ),
                )
            }
            atualizarLista()
            val sucesso =
                if (estavaEditando) {
                    "Estoque atualizado com sucesso."
                } else {
                    "Estoque cadastrado com sucesso."
                }
            limparCampos()
            irParaListagem()
            onSuccess(sucesso)
        }
    }

    fun excluir(
        estoque: EstoqueItem,
        onSuccess: (String) -> Unit = {},
    ) {
        if (estoque.quantidadeReservada > 0) {
            mensagem.value = "O estoque não pode ser excluído enquanto houver quantidade reservada."
            return
        }
        executarRequisicao(mensagem, "Não foi possível excluir o estoque") {
            repositorio.excluir(estoque.produtoId)
            atualizarLista()
            onSuccess("Estoque excluído com sucesso.")
        }
    }

    fun editar(estoque: EstoqueItem) {
        produtoId.value = estoque.produtoId.toString()
        quantidadeFisica.value = estoque.quantidadeFisica.toString()
        quantidadeMinima.value = estoque.quantidadeMinima.toString()
        localizacao.value = estoque.localizacao.orEmpty()
        bloqueado.value = estoque.bloqueado
        editando.value = true
        quantidadeReservadaAtual.value = estoque.quantidadeReservada
        erros.value = emptyMap()
        irParaFormulario(estoque.produtoId)
    }

    fun atualizarFormulario(form: EstoqueCadastroForm) {
        produtoId.value = form.produtoId
        quantidadeFisica.value = form.quantidadeFisica
        quantidadeMinima.value = form.quantidadeMinima
        localizacao.value = form.localizacao
        bloqueado.value = form.bloqueado
        if (erros.value.isNotEmpty()) {
            erros.value = validarEstoqueCadastro(form, quantidadeReservadaAtual.value)
        }
    }

    fun limparCampos() {
        produtoId.value = ""
        quantidadeFisica.value = "0"
        quantidadeMinima.value = "0"
        localizacao.value = ""
        bloqueado.value = false
        editando.value = false
        quantidadeReservadaAtual.value = null
        erros.value = emptyMap()
    }

    fun novo() {
        limparCampos()
        irParaFormulario()
    }

    fun irParaFormulario(produtoId: Long? = null) {
        backStack.clear()
        backStack.add(EstoqueRotas.formulario(produtoId))
    }

    fun irParaListagem() {
        backStack.clear()
        backStack.add(EstoqueRotas.listagem)
    }

    fun formulario(): EstoqueCadastroForm =
        EstoqueCadastroForm(
            produtoId = produtoId.value,
            quantidadeFisica = quantidadeFisica.value,
            quantidadeMinima = quantidadeMinima.value,
            localizacao = localizacao.value,
            bloqueado = bloqueado.value,
        )

    private suspend fun atualizarLista() {
        _lista.value = repositorio.listar().map(Estoque::toItem)
    }
}

private fun Estoque.toItem() =
    EstoqueItem(
        produtoId = produtoId,
        quantidadeFisica = quantidadeFisica,
        quantidadeReservada = quantidadeReservada,
        quantidadeDisponivel = quantidadeDisponivel,
        quantidadeMinima = quantidadeMinima,
        localizacao = localizacao,
        bloqueado = bloqueado,
    )
