package org.greenhouse.app.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.navigation3.runtime.NavBackStack
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.greenhouse.app.model.Desconto
import org.greenhouse.app.model.DescontoRequest
import org.greenhouse.app.model.TipoDesconto as TipoDescontoApi
import org.greenhouse.app.repository.DescontoRepositorioRemoto
import org.greenhouse.ui.navigation.DescontoRotas
import org.greenhouse.ui.screens.desconto.model.DescontoCadastroForm
import org.greenhouse.ui.screens.desconto.model.DescontoItem
import org.greenhouse.ui.screens.desconto.model.TipoDescontoCadastro
import org.greenhouse.ui.screens.desconto.validation.toDescontoCadastroDataOrNull
import org.greenhouse.ui.screens.desconto.validation.validarDescontoCadastro
import kotlin.math.roundToLong

class DescontoViewModel(
    private val repositorio: DescontoRepositorioRemoto,
) : ViewModel() {
    private val _lista = MutableStateFlow<List<DescontoItem>>(emptyList())
    val lista: StateFlow<List<DescontoItem>> = _lista.asStateFlow()

    var id: MutableState<String> = mutableStateOf("")
    var titulo: MutableState<String> = mutableStateOf("")
    var descricao: MutableState<String> = mutableStateOf("")
    var tipo: MutableState<TipoDescontoCadastro> = mutableStateOf(TipoDescontoCadastro.PERCENTUAL)
    var valor: MutableState<String> = mutableStateOf("")
    var dataInicio: MutableState<String> = mutableStateOf("")
    var dataFim: MutableState<String> = mutableStateOf("")
    var ativo: MutableState<Boolean> = mutableStateOf(true)
    var quantidadeMinimaItens: MutableState<String> = mutableStateOf("1")
    var erros: MutableState<Map<String, String>> = mutableStateOf(emptyMap())
    var mensagem: MutableState<String> = mutableStateOf("")

    val backStack = NavBackStack<DescontoRotas>(DescontoRotas.listagem)

    fun carregar() {
        executarRequisicao(mensagem, "Não foi possível carregar os descontos") {
            atualizarLista()
        }
    }

    fun gravar(onSuccess: (String) -> Unit = {}) {
        val form = formulario()
        erros.value = validarDescontoCadastro(form)
        val dados = form.toDescontoCadastroDataOrNull()
        if (dados == null) {
            mensagem.value = "Corrija os campos destacados"
            return
        }

        val idEdicao = id.value.toLongOrNull()
        val desconto =
            DescontoRequest(
                titulo = dados.titulo,
                descricao = dados.descricao,
                tipo = TipoDescontoApi.valueOf(dados.tipo.name),
                valor = dados.valor.toDouble(),
                dataInicio = dados.dataInicio,
                dataFim = dados.dataFim,
                ativo = dados.ativo,
                quantidadeMinimaItens = dados.quantidadeMinimaItens.toInt(),
            )

        executarRequisicao(mensagem, "Não foi possível gravar o desconto") {
            if (idEdicao == null) {
                repositorio.salvar(desconto)
            } else {
                repositorio.atualizar(idEdicao, desconto)
            }
            atualizarLista()
            val sucesso =
                if (idEdicao == null) {
                    "Desconto cadastrado com sucesso."
                } else {
                    "Desconto atualizado com sucesso."
                }
            limparCampos()
            irParaListagem()
            onSuccess(sucesso)
        }
    }

    fun excluir(
        desconto: DescontoItem,
        onSuccess: (String) -> Unit = {},
    ) {
        executarRequisicao(mensagem, "Não foi possível inativar o desconto") {
            repositorio.excluir(desconto.id)
            atualizarLista()
            onSuccess("Desconto inativado com sucesso.")
        }
    }

    fun editar(desconto: DescontoItem) {
        id.value = desconto.id.toString()
        titulo.value = desconto.titulo
        descricao.value = desconto.descricao.orEmpty()
        tipo.value = desconto.tipo
        valor.value = desconto.valor.replace('.', ',')
        dataInicio.value = desconto.dataInicio.toDataBrasileira()
        dataFim.value = desconto.dataFim.toDataBrasileira()
        ativo.value = desconto.ativo
        quantidadeMinimaItens.value = desconto.quantidadeMinimaItens.toString()
        erros.value = emptyMap()
        irParaFormulario(desconto.id)
    }

    fun atualizarFormulario(form: DescontoCadastroForm) {
        titulo.value = form.titulo
        descricao.value = form.descricao
        tipo.value = form.tipo
        valor.value = form.valor
        dataInicio.value = form.dataInicio
        dataFim.value = form.dataFim
        ativo.value = form.ativo
        quantidadeMinimaItens.value = form.quantidadeMinimaItens
        if (erros.value.isNotEmpty()) {
            erros.value = validarDescontoCadastro(form)
        }
    }

    fun limparCampos() {
        id.value = ""
        titulo.value = ""
        descricao.value = ""
        tipo.value = TipoDescontoCadastro.PERCENTUAL
        valor.value = ""
        dataInicio.value = ""
        dataFim.value = ""
        ativo.value = true
        quantidadeMinimaItens.value = "1"
        erros.value = emptyMap()
    }

    fun novo() {
        limparCampos()
        irParaFormulario()
    }

    fun irParaFormulario(descontoId: Long? = null) {
        backStack.clear()
        backStack.add(DescontoRotas.formulario(descontoId))
    }

    fun irParaListagem() {
        backStack.clear()
        backStack.add(DescontoRotas.listagem)
    }

    fun formulario(): DescontoCadastroForm =
        DescontoCadastroForm(
            titulo = titulo.value,
            descricao = descricao.value,
            tipo = tipo.value,
            valor = valor.value,
            dataInicio = dataInicio.value,
            dataFim = dataFim.value,
            ativo = ativo.value,
            quantidadeMinimaItens = quantidadeMinimaItens.value,
        )

    private suspend fun atualizarLista() {
        _lista.value = repositorio.listar().map(Desconto::toItem)
    }
}

private fun Desconto.toItem() =
    DescontoItem(
        id = id,
        titulo = titulo,
        descricao = descricao,
        tipo = TipoDescontoCadastro.valueOf(tipo.name),
        valor = valor.formatarValorDesconto(),
        dataInicio = dataInicio,
        dataFim = dataFim,
        ativo = ativo,
        quantidadeMinimaItens = quantidadeMinimaItens,
    )

private fun Double.formatarValorDesconto(): String {
    val centavos = (this * 100).roundToLong()
    return "${centavos / 100}.${(centavos % 100).toString().padStart(2, '0')}"
}

private fun String?.toDataBrasileira(): String {
    if (this == null) return ""
    val data = substringBefore('T')
    val partes = data.split('-')
    if (partes.size != 3) return ""
    return "${partes[2]}/${partes[1]}/${partes[0]}"
}
