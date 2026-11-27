package org.greenhouse.app.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.navigation3.runtime.NavBackStack
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.greenhouse.app.model.Especie
import org.greenhouse.app.model.EspecieRequest
import org.greenhouse.app.repository.EspecieRepositorioRemoto
import org.greenhouse.ui.navigation.EspecieRotas
import org.greenhouse.ui.screens.especie.model.EspecieCadastroForm
import org.greenhouse.ui.screens.especie.model.EspecieItem
import org.greenhouse.ui.screens.especie.validation.toEspecieCadastroDataOrNull
import org.greenhouse.ui.screens.especie.validation.validarEspecieCadastro
import kotlin.math.roundToLong

class EspecieViewModel(
    private val repositorio: EspecieRepositorioRemoto,
) : ViewModel() {
    private val _lista = MutableStateFlow<List<EspecieItem>>(emptyList())
    val lista: StateFlow<List<EspecieItem>> = _lista.asStateFlow()

    var id: MutableState<String> = mutableStateOf("")
    var nomePopular: MutableState<String> = mutableStateOf("")
    var nomeCientifico: MutableState<String> = mutableStateOf("")
    var descricao: MutableState<String> = mutableStateOf("")
    var intervaloRegaDias: MutableState<String> = mutableStateOf("")
    var temperaturaMin: MutableState<String> = mutableStateOf("")
    var temperaturaMax: MutableState<String> = mutableStateOf("")
    var umidadeArMin: MutableState<String> = mutableStateOf("")
    var umidadeArMax: MutableState<String> = mutableStateOf("")
    var umidadeSoloMin: MutableState<String> = mutableStateOf("")
    var umidadeSoloMax: MutableState<String> = mutableStateOf("")
    var editando: MutableState<Boolean> = mutableStateOf(false)
    var erros: MutableState<Map<String, String>> = mutableStateOf(emptyMap())
    var mensagem: MutableState<String> = mutableStateOf("")

    val backStack = NavBackStack<EspecieRotas>(EspecieRotas.listagem)

    fun carregar() {
        executarRequisicao(mensagem, "Não foi possível carregar as espécies") {
            atualizarLista()
        }
    }

    fun gravar(onSuccess: (String) -> Unit = {}) {
        val form = formulario()
        erros.value = validarEspecieCadastro(form)
        val dados = form.toEspecieCadastroDataOrNull()
        if (dados == null) {
            mensagem.value = "Corrija os campos destacados"
            return
        }

        val idEdicao = id.value.toLongOrNull()
        val request =
            EspecieRequest(
                nomePopular = dados.nomePopular,
                nomeCientifico = dados.nomeCientifico,
                descricao = dados.descricao,
                intervaloRegaDias = dados.intervaloRegaDias,
                temperaturaMin = dados.temperaturaMin,
                temperaturaMax = dados.temperaturaMax,
                umidadeArMin = dados.umidadeArMin,
                umidadeArMax = dados.umidadeArMax,
                umidadeSoloMin = dados.umidadeSoloMin,
                umidadeSoloMax = dados.umidadeSoloMax,
            )

        executarRequisicao(mensagem, "Não foi possível gravar a espécie") {
            if (idEdicao == null) {
                repositorio.salvar(request)
            } else {
                repositorio.atualizar(idEdicao, request)
            }
            atualizarLista()
            val sucesso =
                if (idEdicao == null) {
                    "Espécie cadastrada com sucesso."
                } else {
                    "Espécie atualizada com sucesso."
                }
            limparCampos()
            irParaListagem()
            onSuccess(sucesso)
        }
    }

    fun excluir(
        especie: EspecieItem,
        onSuccess: (String) -> Unit = {},
    ) {
        executarRequisicao(mensagem, "Não foi possível excluir a espécie") {
            repositorio.excluir(especie.id)
            atualizarLista()
            onSuccess("Espécie excluída com sucesso.")
        }
    }

    fun alternarStatus(
        especie: EspecieItem,
        onSuccess: (String) -> Unit = {},
    ) {
        executarRequisicao(mensagem, "Não foi possível alterar o status da espécie") {
            repositorio.alternarStatus(especie.id)
            atualizarLista()
            onSuccess("Status da espécie atualizado.")
        }
    }

    fun editar(especie: EspecieItem) {
        id.value = especie.id.toString()
        nomePopular.value = especie.nomePopular
        nomeCientifico.value = especie.nomeCientifico.orEmpty()
        descricao.value = especie.descricao.orEmpty()
        intervaloRegaDias.value = especie.intervaloRegaDias?.toString().orEmpty()
        temperaturaMin.value = especie.temperaturaMin.paraCampoDecimal()
        temperaturaMax.value = especie.temperaturaMax.paraCampoDecimal()
        umidadeArMin.value = especie.umidadeArMin.paraCampoDecimal()
        umidadeArMax.value = especie.umidadeArMax.paraCampoDecimal()
        umidadeSoloMin.value = especie.umidadeSoloMin.paraCampoDecimal()
        umidadeSoloMax.value = especie.umidadeSoloMax.paraCampoDecimal()
        editando.value = true
        erros.value = emptyMap()
        irParaFormulario(especie.id)
    }

    fun atualizarFormulario(form: EspecieCadastroForm) {
        nomePopular.value = form.nomePopular
        nomeCientifico.value = form.nomeCientifico
        descricao.value = form.descricao
        intervaloRegaDias.value = form.intervaloRegaDias
        temperaturaMin.value = form.temperaturaMin
        temperaturaMax.value = form.temperaturaMax
        umidadeArMin.value = form.umidadeArMin
        umidadeArMax.value = form.umidadeArMax
        umidadeSoloMin.value = form.umidadeSoloMin
        umidadeSoloMax.value = form.umidadeSoloMax
        if (erros.value.isNotEmpty()) {
            erros.value = validarEspecieCadastro(form)
        }
    }

    fun limparCampos() {
        id.value = ""
        nomePopular.value = ""
        nomeCientifico.value = ""
        descricao.value = ""
        intervaloRegaDias.value = ""
        temperaturaMin.value = ""
        temperaturaMax.value = ""
        umidadeArMin.value = ""
        umidadeArMax.value = ""
        umidadeSoloMin.value = ""
        umidadeSoloMax.value = ""
        editando.value = false
        erros.value = emptyMap()
    }

    fun novo() {
        limparCampos()
        irParaFormulario()
    }

    fun irParaFormulario(especieId: Long? = null) {
        backStack.clear()
        backStack.add(EspecieRotas.formulario(especieId))
    }

    fun irParaListagem() {
        backStack.clear()
        backStack.add(EspecieRotas.listagem)
    }

    fun formulario(): EspecieCadastroForm =
        EspecieCadastroForm(
            nomePopular = nomePopular.value,
            nomeCientifico = nomeCientifico.value,
            descricao = descricao.value,
            intervaloRegaDias = intervaloRegaDias.value,
            temperaturaMin = temperaturaMin.value,
            temperaturaMax = temperaturaMax.value,
            umidadeArMin = umidadeArMin.value,
            umidadeArMax = umidadeArMax.value,
            umidadeSoloMin = umidadeSoloMin.value,
            umidadeSoloMax = umidadeSoloMax.value,
        )

    private suspend fun atualizarLista() {
        _lista.value = repositorio.listar().map(Especie::toItem)
    }
}

private fun Especie.toItem() =
    EspecieItem(
        id = id,
        nomePopular = nomePopular,
        nomeCientifico = nomeCientifico,
        descricao = descricao,
        intervaloRegaDias = intervaloRegaDias,
        ativo = ativo,
        temperaturaMin = temperaturaMin,
        temperaturaMax = temperaturaMax,
        umidadeArMin = umidadeArMin,
        umidadeArMax = umidadeArMax,
        umidadeSoloMin = umidadeSoloMin,
        umidadeSoloMax = umidadeSoloMax,
    )

private fun Double?.paraCampoDecimal(): String {
    if (this == null) return ""
    val centesimos = (this * 100).roundToLong()
    val sinal = if (centesimos < 0) "-" else ""
    val absoluto = kotlin.math.abs(centesimos)
    return "$sinal${absoluto / 100}.${(absoluto % 100).toString().padStart(2, '0')}"
}
