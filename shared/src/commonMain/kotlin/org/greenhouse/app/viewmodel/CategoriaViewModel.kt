package org.greenhouse.app.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.navigation3.runtime.NavBackStack
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.greenhouse.app.model.Categoria
import org.greenhouse.app.repository.CategoriaRepositorioRemoto
import org.greenhouse.ui.navigation.CategoriaRotas
import org.greenhouse.ui.screens.categoria.model.CategoriaCadastroForm
import org.greenhouse.ui.screens.categoria.validation.toCategoriaCadastroDataOrNull
import org.greenhouse.ui.screens.categoria.validation.validarCategoriaCadastro

class CategoriaViewModel(
    private val repositorio: CategoriaRepositorioRemoto,
) : ViewModel() {
    private val _lista: MutableStateFlow<List<Categoria>> = MutableStateFlow(emptyList())
    val lista: StateFlow<List<Categoria>> = _lista.asStateFlow()

    var id: MutableState<String> = mutableStateOf("")
    var nome: MutableState<String> = mutableStateOf("")
    var descricao: MutableState<String> = mutableStateOf("")
    var ativo: MutableState<Boolean> = mutableStateOf(true)
    var observacao: MutableState<String> = mutableStateOf("")
    var dataCadastro: MutableState<String> = mutableStateOf("")
    var erros: MutableState<Map<String, String>> = mutableStateOf(emptyMap())
    var mensagem: MutableState<String> = mutableStateOf("")

    val backStack = NavBackStack<CategoriaRotas>(CategoriaRotas.listagem)

    fun carregar() {
        executarRequisicao(mensagem, "Não foi possível carregar as categorias") {
            _lista.value = repositorio.listar()
        }
    }

    fun gravar() {
        val form = formulario()
        erros.value = validarCategoriaCadastro(form)
        val dados = form.toCategoriaCadastroDataOrNull()
        if (dados == null) {
            mensagem.value = "Corrija os campos destacados"
            return
        }
        val idEdicao = id.value.toLongOrNull()
        val categoria =
            Categoria(
                id = idEdicao ?: 0,
                nome = dados.nome,
                descricao = dados.descricao,
                ativo = dados.ativo,
                observacao = dados.observacao,
                dataCadastro = dados.dataCadastro,
            )
        executarRequisicao(mensagem, "Não foi possível gravar a categoria") {
            if (idEdicao != null) {
                repositorio.atualizar(categoria)
                mensagem.value = "Categoria $idEdicao atualizada com sucesso!"
            } else {
                repositorio.salvar(categoria)
                mensagem.value = "Categoria cadastrada com sucesso!"
            }
            limparCampos()
            irParaListagem()
            _lista.value = repositorio.listar()
        }
    }

    fun apagar(id: Long) {
        executarRequisicao(mensagem, "Não foi possível apagar a categoria") {
            repositorio.apagar(id)
            mensagem.value = "Categoria $id apagada com sucesso!"
            _lista.value = repositorio.listar()
        }
    }

    fun editar(categoria: Categoria) {
        id.value = categoria.id.toString()
        nome.value = categoria.nome
        descricao.value = categoria.descricao
        ativo.value = categoria.ativo
        observacao.value = categoria.observacao ?: ""
        dataCadastro.value = isoParaDataBrasileira(categoria.dataCadastro)
        erros.value = emptyMap()
        mensagem.value = "Editando categoria ${categoria.id}"
        irParaFormulario()
    }

    fun limparCampos() {
        id.value = ""
        nome.value = ""
        descricao.value = ""
        ativo.value = true
        observacao.value = ""
        dataCadastro.value = ""
        erros.value = emptyMap()
    }

    fun irParaFormulario() {
        backStack.clear()
        backStack.add(CategoriaRotas.formulario)
    }

    fun irParaListagem() {
        backStack.clear()
        backStack.add(CategoriaRotas.listagem)
    }

    private fun formulario() =
        CategoriaCadastroForm(
            nome = nome.value,
            descricao = descricao.value,
            ativo = ativo.value,
            observacao = observacao.value,
            dataCadastro = dataCadastro.value,
        )
}

fun isoParaDataBrasileira(data: String?): String {
    if (data == null || data.length < 10) return ""
    return "${data.substring(8, 10)}/${data.substring(5, 7)}/${data.substring(0, 4)}"
}
