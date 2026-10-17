package org.greenhouse.ui.screens.desconto.validation

import org.greenhouse.ui.screens.desconto.model.DescontoCadastroForm
import org.greenhouse.ui.screens.desconto.model.TipoDescontoCadastro

fun formatarDataBrasileira(entrada: String): String {
    val digitos = entrada.filter(Char::isDigit).take(8)
    return when {
        digitos.length <= 2 -> digitos
        digitos.length <= 4 -> "${digitos.take(2)}/${digitos.drop(2)}"
        else -> "${digitos.take(2)}/${digitos.substring(2, 4)}/${digitos.drop(4)}"
    }
}

fun formatarValorDesconto(entrada: String): String {
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

fun validarDescontoCadastro(form: DescontoCadastroForm): Map<String, String> {
    val erros = mutableMapOf<String, String>()
    val titulo = form.titulo.trim()
    val descricao = form.descricao.trim()
    val valor = form.valor.trim().replace(',', '.')
    val quantidadeMinimaItens = form.quantidadeMinimaItens.trim().toIntOrNull()

    if (titulo.isEmpty()) {
        erros["titulo"] = "O título do desconto é obrigatório"
    } else if (titulo.length > 150) {
        erros["titulo"] = "O título deve ter no máximo 150 caracteres"
    }

    if (descricao.length > 1000) {
        erros["descricao"] = "A descrição deve ter no máximo 1000 caracteres"
    }

    if (form.valor.isBlank()) {
        erros["valor"] = "O valor do desconto é obrigatório"
    } else if (!valor.matches(Regex("""\d+(\.\d+)?"""))) {
        erros["valor"] = "Informe um valor numérico válido"
    } else if (valor.substringAfter('.', "").length > 2) {
        erros["valor"] = "O valor deve ter no máximo 2 casas decimais"
    } else if (valor.compareToDecimal("0") <= 0) {
        erros["valor"] = "O valor do desconto deve ser maior que zero"
    } else if (valor.compareToDecimal("9999999999.99") > 0) {
        erros["valor"] = "O valor deve ter no máximo 10 dígitos inteiros"
    } else if (form.tipo == TipoDescontoCadastro.PERCENTUAL && valor.compareToDecimal("100") > 0) {
        erros["valor"] = "O desconto percentual não pode ser maior que 100"
    }

    if (quantidadeMinimaItens == null || quantidadeMinimaItens < 1) {
        erros["quantidadeMinimaItens"] = "A quantidade mínima deve ser um número maior que zero"
    }

    val dataInicioIso = form.dataInicio.toIsoDateOrNull()
    val dataFimIso = form.dataFim.toIsoDateOrNull()
    val dataInicioValida = form.dataInicio.isBlank() || dataInicioIso != null
    val dataFimValida = form.dataFim.isBlank() || dataFimIso != null
    if (!dataInicioValida) {
        erros["dataInicio"] = "Selecione uma data inicial válida"
    }
    if (!dataFimValida) {
        erros["dataFim"] = "Selecione uma data final válida"
    }
    if (
        dataInicioValida &&
        dataFimValida &&
        form.dataInicio.isNotBlank() &&
        form.dataFim.isNotBlank() &&
        dataInicioIso != null &&
        dataFimIso != null &&
        dataFimIso < dataInicioIso
    ) {
        erros["dataFim"] = "A data final não pode ser anterior à data inicial"
    }

    return erros
}

fun String.toOffsetDateTimeOrNull(): String? {
    val isoDate = toIsoDateOrNull() ?: return null
    return "${isoDate}T00:00:00Z"
}

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

private fun String.compareToDecimal(other: String): Int {
    val thisParts = split('.', limit = 2)
    val otherParts = other.split('.', limit = 2)
    val thisInteger = thisParts[0].trimStart('0').ifEmpty { "0" }
    val otherInteger = otherParts[0].trimStart('0').ifEmpty { "0" }

    if (thisInteger.length != otherInteger.length) {
        return thisInteger.length.compareTo(otherInteger.length)
    }
    val integerComparison = thisInteger.compareTo(otherInteger)
    if (integerComparison != 0) return integerComparison

    val thisFraction = thisParts.getOrElse(1) { "" }.padEnd(2, '0')
    val otherFraction = otherParts.getOrElse(1) { "" }.padEnd(2, '0')
    return thisFraction.compareTo(otherFraction)
}

private fun Int.isLeapYear(): Boolean = this % 4 == 0 && (this % 100 != 0 || this % 400 == 0)
