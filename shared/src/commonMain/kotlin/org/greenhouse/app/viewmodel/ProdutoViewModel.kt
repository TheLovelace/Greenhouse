package org.greenhouse.app.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.navigation3.runtime.NavBackStack
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.greenhouse.app.model.Produto
import org.greenhouse.app.repository.ProdutoRepositorioRemoto
import org.greenhouse.ui.navigation.ProdutoRotas
import org.greenhouse.ui.screens.produto.model.ProdutoCadastroForm
import org.greenhouse.ui.screens.produto.validation.toProdutoCadastroDataOrNull
import org.greenhouse.ui.screens.produto.validation.validarProdutoCadastro
import kotlin.math.roundToLong

class ProdutoViewModel(
    private val repositorio: ProdutoRepositorioRemoto,
) : ViewModel() {
    private val _lista: MutableStateFlow<List<Produto>> = MutableStateFlow(emptyList())
    val lista: StateFlow<List<Produto>> = _lista.asStateFlow()

    var id: MutableState<String> = mutableStateOf("")
    var codigo: MutableState<String> = mutableStateOf("")
    var nome: MutableState<String> = mutableStateOf("")
    var descricao: MutableState<String> = mutableStateOf("")
    var preco: MutableState<String> = mutableStateOf("")
    var ativo: MutableState<Boolean> = mutableStateOf(true)
    var prazoPreparoMinutos: MutableState<String> = mutableStateOf("")
    var erros: MutableState<Map<String, String>> = mutableStateOf(emptyMap())
    var mensagem: MutableState<String> = mutableStateOf("")

    val backStack = NavBackStack<ProdutoRotas>(ProdutoRotas.listagem)

    fun carregar() {
        executarRequisicao(mensagem, "Não foi possível carregar os produtos") {
            _lista.value = repositorio.listar()
        }
    }

    fun gravar() {
        val form = formulario()
        erros.value = validarProdutoCadastro(form)
        val dados = form.toProdutoCadastroDataOrNull()
        if (dados == null) {
            mensagem.value = "Corrija os campos destacados"
            return
        }
        val idEdicao = id.value.toLongOrNull()
        val produto =
            Produto(
                id = idEdicao ?: 0,
                codigo = dados.codigo,
                nome = dados.nome,
                descricao = dados.descricao,
                preco = dados.preco.toDouble(),
                ativo = dados.ativo,
                prazoPreparoMinutos = dados.prazoPreparoMinutos,
            )
        executarRequisicao(mensagem, "Não foi possível gravar o produto") {
            if (idEdicao != null) {
                repositorio.atualizar(produto)
                mensagem.value = "Produto $idEdicao atualizado com sucesso!"
            } else {
                repositorio.salvar(produto)
                mensagem.value = "Produto cadastrado com sucesso!"
            }
            limparCampos()
            irParaListagem()
            _lista.value = repositorio.listar()
        }
    }

    fun apagar(id: Long) {
        executarRequisicao(mensagem, "Não foi possível apagar o produto") {
            repositorio.apagar(id)
            mensagem.value = "Produto $id apagado com sucesso!"
            _lista.value = repositorio.listar()
        }
    }

    fun editar(produto: Produto) {
        id.value = produto.id.toString()
        codigo.value = produto.codigo ?: ""
        nome.value = produto.nome
        descricao.value = produto.descricao ?: ""
        preco.value = formatarPreco(produto.preco)
        ativo.value = produto.ativo
        prazoPreparoMinutos.value = produto.prazoPreparoMinutos?.toString() ?: ""
        erros.value = emptyMap()
        mensagem.value = "Editando produto ${produto.id}"
        irParaFormulario()
    }

    fun limparCampos() {
        id.value = ""
        codigo.value = ""
        nome.value = ""
        descricao.value = ""
        preco.value = ""
        ativo.value = true
        prazoPreparoMinutos.value = ""
        erros.value = emptyMap()
    }

    fun irParaFormulario() {
        backStack.clear()
        backStack.add(ProdutoRotas.formulario)
    }

    fun irParaListagem() {
        backStack.clear()
        backStack.add(ProdutoRotas.listagem)
    }

    private fun formulario() =
        ProdutoCadastroForm(
            codigo = codigo.value,
            nome = nome.value,
            descricao = descricao.value,
            preco = preco.value,
            ativo = ativo.value,
            prazoPreparoMinutos = prazoPreparoMinutos.value,
        )
}

fun formatarPreco(preco: Double): String {
    val centavos = (preco * 100).roundToLong()
    return "${centavos / 100},${(centavos % 100).toString().padStart(2, '0')}"
}
