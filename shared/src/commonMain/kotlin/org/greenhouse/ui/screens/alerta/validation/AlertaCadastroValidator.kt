package org.greenhouse.ui.screens.alerta.validation

import org.greenhouse.ui.screens.alerta.model.AlertaCadastroData
import org.greenhouse.ui.screens.alerta.model.AlertaCadastroForm

fun formatarIdAlerta(texto: String): String = texto.filter(Char::isDigit)

fun formatarPrioridadeAlerta(texto: String): String = texto.filter(Char::isDigit).take(1)

fun validarAlertaCadastro(form: AlertaCadastroForm): Map<String, String> {
    val erros = mutableMapOf<String, String>()
    val dispositivoId = form.dispositivoIotId.trim().toLongOrNull()
    val especieId = form.especieId.trim().toLongOrNull()
    val titulo = form.titulo.trim()
    val mensagem = form.mensagem.trim()
    val prioridade = form.prioridade.trim().toIntOrNull()

    if (form.dispositivoIotId.isBlank()) {
        erros["dispositivoIotId"] = "O ID do dispositivo IoT é obrigatório"
    } else if (dispositivoId == null || dispositivoId <= 0L) {
        erros["dispositivoIotId"] = "Informe um ID de dispositivo maior que zero"
    }

    if (form.especieId.isBlank()) {
        erros["especieId"] = "O ID da espécie é obrigatório"
    } else if (especieId == null || especieId <= 0L) {
        erros["especieId"] = "Informe um ID de espécie maior que zero"
    }

    if (titulo.isEmpty()) {
        erros["titulo"] = "O título do alerta é obrigatório"
    } else if (titulo.length > 150) {
        erros["titulo"] = "O título deve ter no máximo 150 caracteres"
    }

    if (mensagem.length > 1000) {
        erros["mensagem"] = "A mensagem deve ter no máximo 1000 caracteres"
    }

    if (prioridade == null || prioridade !in 1..3) {
        erros["prioridade"] = "Selecione uma prioridade válida"
    }

    if (!form.monitorarTemperatura && !form.monitorarUmidadeAr && !form.monitorarUmidadeSolo) {
        erros["condicoes"] = "Selecione ao menos uma condição monitorada"
    }

    return erros
}

fun AlertaCadastroForm.toAlertaCadastroDataOrNull(): AlertaCadastroData? {
    if (validarAlertaCadastro(this).isNotEmpty()) return null
    return AlertaCadastroData(
        dispositivoIotId = requireNotNull(dispositivoIotId.trim().toLongOrNull()),
        especieId = requireNotNull(especieId.trim().toLongOrNull()),
        titulo = titulo.trim(),
        mensagem = mensagem.trim().ifEmpty { null },
        prioridade = requireNotNull(prioridade.trim().toIntOrNull()),
        monitorarTemperatura = monitorarTemperatura,
        monitorarUmidadeAr = monitorarUmidadeAr,
        monitorarUmidadeSolo = monitorarUmidadeSolo,
    )
}
