package org.greenhouse.app.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.navigation3.runtime.NavBackStack
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.greenhouse.app.model.Alerta
import org.greenhouse.app.model.AlertaRequest
import org.greenhouse.app.model.DispositivoIotResumo
import org.greenhouse.app.repository.AlertaRepositorioRemoto
import org.greenhouse.ui.navigation.AlertaRotas
import org.greenhouse.ui.screens.alerta.model.AlertaCadastroForm
import org.greenhouse.ui.screens.alerta.model.AlertaItem
import org.greenhouse.ui.screens.alerta.validation.toAlertaCadastroDataOrNull
import org.greenhouse.ui.screens.alerta.validation.validarAlertaCadastro

class AlertaViewModel(
    private val repositorio: AlertaRepositorioRemoto,
) : ViewModel() {
    private val _lista = MutableStateFlow<List<AlertaItem>>(emptyList())
    val lista: StateFlow<List<AlertaItem>> = _lista.asStateFlow()

    private val _dispositivos = MutableStateFlow<List<DispositivoIotResumo>>(emptyList())
    val dispositivos: StateFlow<List<DispositivoIotResumo>> = _dispositivos.asStateFlow()

    var id: MutableState<String> = mutableStateOf("")
    var dispositivoIotId: MutableState<String> = mutableStateOf("")
    var especieId: MutableState<String> = mutableStateOf("")
    var titulo: MutableState<String> = mutableStateOf("")
    var mensagemAlerta: MutableState<String> = mutableStateOf("")
    var prioridade: MutableState<String> = mutableStateOf("2")
    var monitorarTemperatura: MutableState<Boolean> = mutableStateOf(true)
    var monitorarUmidadeAr: MutableState<Boolean> = mutableStateOf(false)
    var monitorarUmidadeSolo: MutableState<Boolean> = mutableStateOf(false)
    var editando: MutableState<Boolean> = mutableStateOf(false)
    var erros: MutableState<Map<String, String>> = mutableStateOf(emptyMap())
    var mensagem: MutableState<String> = mutableStateOf("")

    val backStack = NavBackStack<AlertaRotas>(AlertaRotas.listagem)

    fun carregar() {
        executarRequisicao(mensagem, "Não foi possível carregar os alertas IoT") {
            atualizarLista()
            _dispositivos.value = runCatching { repositorio.listarDispositivos() }.getOrDefault(emptyList())
        }
    }

    fun gravar(onSuccess: (String) -> Unit = {}) {
        val form = formulario()
        erros.value = validarAlertaCadastro(form)
        val dados = form.toAlertaCadastroDataOrNull()
        if (dados == null) {
            mensagem.value = "Corrija os campos destacados"
            return
        }

        val idEdicao = id.value.toLongOrNull()
        val request =
            AlertaRequest(
                dispositivoIotId = dados.dispositivoIotId,
                especieId = dados.especieId,
                titulo = dados.titulo,
                mensagem = dados.mensagem,
                prioridade = dados.prioridade,
                monitorarTemperatura = dados.monitorarTemperatura,
                monitorarUmidadeAr = dados.monitorarUmidadeAr,
                monitorarUmidadeSolo = dados.monitorarUmidadeSolo,
            )

        executarRequisicao(mensagem, "Não foi possível gravar o alerta IoT") {
            if (idEdicao == null) {
                repositorio.salvar(request)
            } else {
                repositorio.atualizar(idEdicao, request)
            }
            atualizarLista()
            val sucesso =
                if (idEdicao == null) {
                    "Alerta IoT cadastrado com sucesso."
                } else {
                    "Alerta IoT atualizado com sucesso."
                }
            limparCampos()
            irParaListagem()
            onSuccess(sucesso)
        }
    }

    fun alternarStatus(
        alerta: AlertaItem,
        onSuccess: (String) -> Unit = {},
    ) {
        executarRequisicao(mensagem, "Não foi possível alterar o status do alerta") {
            repositorio.alternarStatus(alerta.id)
            atualizarLista()
            onSuccess(
                if (alerta.ativo) {
                    "Alerta IoT inativado com sucesso."
                } else {
                    "Alerta IoT ativado com sucesso."
                },
            )
        }
    }

    fun editar(alerta: AlertaItem) {
        id.value = alerta.id.toString()
        dispositivoIotId.value = alerta.dispositivoIotId.toString()
        especieId.value = alerta.especieId.toString()
        titulo.value = alerta.titulo
        mensagemAlerta.value = alerta.mensagem.orEmpty()
        prioridade.value = alerta.prioridade.toString()
        monitorarTemperatura.value = alerta.monitorarTemperatura
        monitorarUmidadeAr.value = alerta.monitorarUmidadeAr
        monitorarUmidadeSolo.value = alerta.monitorarUmidadeSolo
        editando.value = true
        erros.value = emptyMap()
        irParaFormulario(alerta.id)
    }

    fun atualizarFormulario(form: AlertaCadastroForm) {
        dispositivoIotId.value = form.dispositivoIotId
        especieId.value = form.especieId
        titulo.value = form.titulo
        mensagemAlerta.value = form.mensagem
        prioridade.value = form.prioridade
        monitorarTemperatura.value = form.monitorarTemperatura
        monitorarUmidadeAr.value = form.monitorarUmidadeAr
        monitorarUmidadeSolo.value = form.monitorarUmidadeSolo
        if (erros.value.isNotEmpty()) {
            erros.value = validarAlertaCadastro(form)
        }
    }

    fun limparCampos() {
        id.value = ""
        dispositivoIotId.value = ""
        especieId.value = ""
        titulo.value = ""
        mensagemAlerta.value = ""
        prioridade.value = "2"
        monitorarTemperatura.value = true
        monitorarUmidadeAr.value = false
        monitorarUmidadeSolo.value = false
        editando.value = false
        erros.value = emptyMap()
    }

    fun novo() {
        limparCampos()
        irParaFormulario()
    }

    fun irParaFormulario(alertaId: Long? = null) {
        backStack.clear()
        backStack.add(AlertaRotas.formulario(alertaId))
    }

    fun irParaListagem() {
        backStack.clear()
        backStack.add(AlertaRotas.listagem)
    }

    fun formulario(): AlertaCadastroForm =
        AlertaCadastroForm(
            dispositivoIotId = dispositivoIotId.value,
            especieId = especieId.value,
            titulo = titulo.value,
            mensagem = mensagemAlerta.value,
            prioridade = prioridade.value,
            monitorarTemperatura = monitorarTemperatura.value,
            monitorarUmidadeAr = monitorarUmidadeAr.value,
            monitorarUmidadeSolo = monitorarUmidadeSolo.value,
        )

    private suspend fun atualizarLista() {
        _lista.value = repositorio.listar().map(Alerta::toItem)
    }
}

private fun Alerta.toItem() =
    AlertaItem(
        id = id,
        dispositivoIotId = dispositivoIotId,
        dispositivoCodigo = dispositivoCodigo,
        dispositivoNome = dispositivoNome,
        especieId = especieId,
        especieNomePopular = especieNomePopular,
        titulo = titulo,
        mensagem = mensagem,
        prioridade = prioridade,
        monitorarTemperatura = monitorarTemperatura,
        monitorarUmidadeAr = monitorarUmidadeAr,
        monitorarUmidadeSolo = monitorarUmidadeSolo,
        ativo = ativo,
    )