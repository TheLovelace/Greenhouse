package org.greenhouse.ui.screens.compraFornecedor.validation

import org.greenhouse.ui.screens.compraFornecedor.model.CompraFornecedorCadastroData
import org.greenhouse.ui.screens.compraFornecedor.model.CompraFornecedorCadastroForm
import org.greenhouse.ui.screens.compraFornecedor.model.CompraItemCadastroData

fun formatarDigitosCompra(entrada: String): String = entrada.filter(Char::isDigit)

fun formatarDataCompra(entrada: String): String {
    val digitos = entrada.filter(Char::isDigit).take(8)
    return when {
        digitos.length <= 2 -> digitos
        digitos.length <= 4 -> "${digitos.take(2)}/${digitos.drop(2)}"
        else -> "${digitos.take(2)}/${digitos.substring(2, 4)}/${digitos.drop(4)}"
    }
}

fun formatarValorCompra(entrada: String): String {
    val resultado = StringBuilder()
    var separadorEncontrado = false
    var casasDecimais = 0

    for (caractere in entrada) {
        when {
            caractere in '0'..'9' && (!separadorEncontrado || casasDecimais < 2) -> {
                resultado.append(caractere)
                if (separadorEncontrado) casasDecimais++
            }
            (caractere == ',' || caractere == '.') && !separadorEncontrado -> {
                resultado.append(caractere)
                separadorEncontrado = true
            }
        }
    }

    return resultado.toString()
}

fun validarCompraFornecedorCadastro(form: CompraFornecedorCadastroForm): Map<String, String> {
    val erros = mutableMapOf<String, String>()
    val funcionarioId = form.funcionarioId.trim()
    val fornecedorId = form.fornecedorId.trim()
    val formaPagamentoId = form.formaPagamentoId.trim()
    val observacao = form.observacao.trim()

    if (funcionarioId.isEmpty()) {
        erros["funcionarioId"] = "O ID do funcionário é obrigatório"
    } else if ((funcionarioId.toLongOrNull() ?: 0L) <= 0L) {
        erros["funcionarioId"] = "Informe um ID de funcionário válido"
    }

    if (fornecedorId.isEmpty()) {
        erros["fornecedorId"] = "O ID do fornecedor é obrigatório"
    } else if ((fornecedorId.toLongOrNull() ?: 0L) <= 0L) {
        erros["fornecedorId"] = "Informe um ID de fornecedor válido"
    }

    if (formaPagamentoId.isEmpty()) {
        erros["formaPagamentoId"] = "O ID da forma de pagamento é obrigatório"
    } else if ((formaPagamentoId.toLongOrNull() ?: 0L) <= 0L) {
        erros["formaPagamentoId"] = "Informe um ID de forma de pagamento válido"
    }

    if (form.dataCompra.isNotBlank() && form.dataCompra.toIsoDateOrNull() == null) {
        erros["dataCompra"] = "Selecione uma data de compra válida"
    }

    if (form.dataEntregaPrevista.isNotBlank() && form.dataEntregaPrevista.toIsoDateOrNull() == null) {
        erros["dataEntregaPrevista"] = "Selecione uma data de entrega válida"
    }

    validarValorMonetario(
        valor = form.valorFrete,
        campo = "valorFrete",
        obrigatorio = false,
        mensagemVazio = "O valor do frete é obrigatório",
        mensagemInvalido = "Informe um valor de frete válido",
        mensagemNegativo = "O valor do frete não pode ser negativo",
        erros = erros,
    )

    if (observacao.length > 1000) {
        erros["observacao"] = "A observação deve ter no máximo 1000 caracteres"
    }

    validarItensCompra(form, erros)

    return erros
}

// O frete só compõe o valor total quando não está incluso nos itens
// (freteIncluso = false), evitando dupla contagem do frete.
fun calcularValorTotalCompra(form: CompraFornecedorCadastroForm): String? {
    if (form.itens.isEmpty()) return null

    var totalCentavos = 0L
    for (item in form.itens) {
        val quantidade = item.quantidade.trim().toIntOrNull() ?: return null
        if (quantidade <= 0) return null
        val unitarioCentavos = item.valorUnitario.toCentavosOrNull() ?: return null
        totalCentavos += unitarioCentavos * quantidade
    }

    if (!form.freteIncluso && form.valorFrete.isNotBlank()) {
        totalCentavos += form.valorFrete.toCentavosOrNull() ?: return null
    }

    return totalCentavos.toDecimal()
}

fun CompraFornecedorCadastroForm.toCompraFornecedorCadastroDataOrNull(): CompraFornecedorCadastroData? {
    if (validarCompraFornecedorCadastro(this).isNotEmpty()) return null

    return CompraFornecedorCadastroData(
        funcionarioId = funcionarioId.trim().toLong(),
        fornecedorId = fornecedorId.trim().toLong(),
        formaPagamentoId = formaPagamentoId.trim().toLong(),
        dataCompra = dataCompra.toIsoDateOrNull(),
        status = status,
        valorTotal = requireNotNull(calcularValorTotalCompra(this)),
        valorFrete = valorFrete.trim().ifEmpty { null }?.toDecimalNormalizado(),
        freteIncluso = freteIncluso,
        dataEntregaPrevista = dataEntregaPrevista.toIsoDateOrNull(),
        observacao = observacao.trim().ifEmpty { null },
        itens =
            itens.map { item ->
                CompraItemCadastroData(
                    produtoId = item.produtoId.trim().toLong(),
                    quantidade = item.quantidade.trim().toInt(),
                    valorUnitario = item.valorUnitario.toDecimalNormalizado(),
                )
            },
    )
}

private fun validarItensCompra(
    form: CompraFornecedorCadastroForm,
    erros: MutableMap<String, String>,
) {
    if (form.itens.isEmpty()) {
        erros["itens"] = "Adicione ao menos um item à compra"
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
            erros["itens[$index].produtoId"] = "O produto já foi adicionado à compra"
        }

        if (quantidadeTexto.isEmpty()) {
            erros["itens[$index].quantidade"] = "A quantidade é obrigatória"
        } else if ((quantidadeTexto.toIntOrNull() ?: 0) < 1) {
            erros["itens[$index].quantidade"] = "A quantidade deve ser maior que zero"
        }

        validarValorMonetario(
            valor = item.valorUnitario,
            campo = "itens[$index].valorUnitario",
            obrigatorio = true,
            mensagemVazio = "O valor unitário é obrigatório",
            mensagemInvalido = "Informe um valor unitário válido",
            mensagemNegativo = "O valor unitário não pode ser negativo",
            erros = erros,
        )
    }
}

private fun validarValorMonetario(
    valor: String,
    campo: String,
    obrigatorio: Boolean,
    mensagemVazio: String,
    mensagemInvalido: String,
    mensagemNegativo: String,
    erros: MutableMap<String, String>,
) {
    val texto = valor.trim().replace(',', '.')
    if (texto.isEmpty()) {
        if (obrigatorio) erros[campo] = mensagemVazio
        return
    }
    if (!texto.matches(Regex("""\d+(\.\d+)?"""))) {
        erros[campo] = mensagemInvalido
        return
    }
    if (texto.substringAfter('.', "").length > 2) {
        erros[campo] = "O valor deve ter no máximo 2 casas decimais"
        return
    }
    val centavos = texto.toCentavosOrNull()
    if (centavos == null) {
        erros[campo] = mensagemInvalido
    } else if (centavos < 0) {
        erros[campo] = mensagemNegativo
    } else if (centavos > 999999999999L) {
        erros[campo] = "O valor deve respeitar o formato NUMERIC(12,2)"
    }
}

private fun String.toCentavosOrNull(): Long? {
    val texto = trim().replace(',', '.')
    if (!texto.matches(Regex("""\d+(\.\d+)?"""))) return null

    val inteiro = texto.substringBefore('.').trimStart('0').ifEmpty { "0" }
    if (inteiro.length > 10) return null
    val fracao = texto.substringAfter('.', "").padEnd(2, '0').take(2)

    return inteiro.toLong() * 100 + fracao.toLong()
}

private fun Long.toDecimal(): String = "${this / 100}.${(this % 100).toString().padStart(2, '0')}"

private fun String.toDecimalNormalizado(): String = requireNotNull(toCentavosOrNull()).toDecimal()

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
