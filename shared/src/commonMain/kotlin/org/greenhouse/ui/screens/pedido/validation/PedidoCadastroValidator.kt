package org.greenhouse.ui.screens.pedido.validation

import org.greenhouse.ui.screens.pedido.model.ModalidadePedidoCadastro
import org.greenhouse.ui.screens.pedido.model.PedidoCadastroData
import org.greenhouse.ui.screens.pedido.model.PedidoCadastroForm
import org.greenhouse.ui.screens.pedido.model.PedidoItemCadastroData
import kotlin.time.Clock

fun formatarDigitos(entrada: String): String = entrada.filter(Char::isDigit)

fun formatarDataBrasileira(entrada: String): String {
    val digitos = entrada.filter(Char::isDigit).take(8)
    return when {
        digitos.length <= 2 -> digitos
        digitos.length <= 4 -> "${digitos.take(2)}/${digitos.drop(2)}"
        else -> "${digitos.take(2)}/${digitos.substring(2, 4)}/${digitos.drop(4)}"
    }
}

fun validarPedidoCadastro(form: PedidoCadastroForm): Map<String, String> {
    val erros = mutableMapOf<String, String>()
    val clienteId = form.clienteId.trim()
    val funcionarioId = form.funcionarioId.trim()
    val descontoId = form.descontoId.trim()
    val formaPagamentoId = form.formaPagamentoId.trim()
    val observacao = form.observacao.trim()
    val numeroVolumes = form.numeroVolumes.trim()

    if (clienteId.isNotEmpty() && (clienteId.toLongOrNull() ?: 0L) <= 0L) {
        erros["clienteId"] = "Informe um ID de cliente válido"
    }

    if (funcionarioId.isEmpty()) {
        erros["funcionarioId"] = "O ID do funcionário é obrigatório"
    } else if ((funcionarioId.toLongOrNull() ?: 0L) <= 0L) {
        erros["funcionarioId"] = "Informe um ID de funcionário válido"
    }

    if (formaPagamentoId.isEmpty()) {
        erros["formaPagamentoId"] = "O ID da forma de pagamento é obrigatório"
    } else if ((formaPagamentoId.toLongOrNull() ?: 0L) <= 0L) {
        erros["formaPagamentoId"] = "Informe um ID de forma de pagamento válido"
    }

    if (descontoId.isNotEmpty() && (descontoId.toLongOrNull() ?: 0L) <= 0L) {
        erros["descontoId"] = "Informe um ID de desconto válido"
    }

    val dataPreenchida = form.dataRetiradaPrevista.isNotBlank()
    val dataIso = form.dataRetiradaPrevista.toIsoDateOrNull()
    if (form.modalidade == ModalidadePedidoCadastro.AGENDADA && !dataPreenchida) {
        erros["dataRetiradaPrevista"] = "A data de retirada é obrigatória para pedidos agendados"
    } else if (dataPreenchida) {
        if (dataIso == null) {
            erros["dataRetiradaPrevista"] = "Selecione uma data de retirada válida"
        } else if (dataIso < hojeIso()) {
            erros["dataRetiradaPrevista"] = "A data de retirada não pode ser anterior a hoje"
        }
    }

    if (observacao.length > 1000) {
        erros["observacao"] = "A observação deve ter no máximo 1000 caracteres"
    }

    if (numeroVolumes.isEmpty()) {
        erros["numeroVolumes"] = "O número de volumes é obrigatório"
    } else if ((numeroVolumes.toIntOrNull() ?: 0) < 1) {
        erros["numeroVolumes"] = "O número de volumes deve ser maior que zero"
    }

    validarItensPedido(form, erros)

    return erros
}

fun PedidoCadastroForm.toPedidoCadastroDataOrNull(): PedidoCadastroData? {
    if (validarPedidoCadastro(this).isNotEmpty()) return null

    return PedidoCadastroData(
        clienteId = clienteId.trim().ifEmpty { null }?.toLongOrNull(),
        funcionarioId = funcionarioId.trim().toLong(),
        descontoId = descontoId.trim().ifEmpty { null }?.toLongOrNull(),
        formaPagamentoId = formaPagamentoId.trim().toLong(),
        modalidade = modalidade,
        dataRetiradaPrevista = dataRetiradaPrevista.toOffsetDateTimeOrNull(),
        observacao = observacao.trim().ifEmpty { null },
        embalagemPresente = embalagemPresente,
        numeroVolumes = numeroVolumes.trim().toInt(),
        itens =
            itens.map { item ->
                PedidoItemCadastroData(
                    produtoId = item.produtoId.trim().toLong(),
                    quantidade = item.quantidade.trim().toInt(),
                )
            },
    )
}

fun String.toOffsetDateTimeOrNull(): String? {
    val isoDate = toIsoDateOrNull() ?: return null
    return "${isoDate}T00:00:00Z"
}

private fun validarItensPedido(
    form: PedidoCadastroForm,
    erros: MutableMap<String, String>,
) {
    if (form.itens.isEmpty()) {
        erros["itens"] = "Adicione ao menos um item ao pedido"
        return
    }

    val produtosAdicionados = mutableSetOf<Long>()

    form.itens.forEachIndexed { index, item ->
        val produtoIdTexto = item.produtoId.trim()
        val quantidadeTexto = item.quantidade.trim()
        val produtoId = produtoIdTexto.toLongOrNull()

        if (produtoIdTexto.isEmpty()) {
            erros["itens[$index].produtoId"] = "O ID do produto é obrigatório"
        } else if (produtoId == null || produtoId <= 0L) {
            erros["itens[$index].produtoId"] = "Informe um ID de produto válido"
        } else if (!produtosAdicionados.add(produtoId)) {
            erros["itens[$index].produtoId"] = "O produto já foi adicionado ao pedido"
        }

        if (quantidadeTexto.isEmpty()) {
            erros["itens[$index].quantidade"] = "A quantidade é obrigatória"
        } else if ((quantidadeTexto.toIntOrNull() ?: 0) < 1) {
            erros["itens[$index].quantidade"] = "A quantidade deve ser maior que zero"
        }
    }
}

private fun hojeIso(): String = Clock.System.now().toString().take(10)

private fun String.toIsoDateOrNull(): String? {
    if (!matches(Regex("""\d{2}/\d{2}/\d{4}"""))) return null

    val day = substring(0, 2).toInt()
    val month = substring(3, 5).toInt()
    val year = substring(6, 10).toInt()
    if (month !in 1..12) return null
    val daysInMonth =
        when (month) {
            2 -> if (year.isLeapYear()) 29 else 28
            4, 6, 9, 11 -> 30
            else -> 31
        }

    if (day !in 1..daysInMonth) return null
    return "${year.toString().padStart(4, '0')}-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
}

private fun Int.isLeapYear(): Boolean = this % 4 == 0 && (this % 100 != 0 || this % 400 == 0)
