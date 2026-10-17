package org.greenhouse.ui.screens.categoria.validation

import org.greenhouse.ui.screens.categoria.model.CategoriaCadastroData
import org.greenhouse.ui.screens.categoria.model.CategoriaCadastroForm

fun validarCategoriaCadastro(form: CategoriaCadastroForm): Map<String, String> {
    val erros = mutableMapOf<String, String>()
    val nome = form.nome.trim()
    val descricao = form.descricao.trim()
    val observacao = form.observacao.trim()
    val dataCadastro = form.dataCadastro.trim()

    if (nome.isEmpty()) {
        erros["nome"] = "O nome da categoria é obrigatório"
    } else if (nome.length > 100) {
        erros["nome"] = "O nome deve ter no máximo 100 caracteres"
    }

    if (descricao.isEmpty()) {
        erros["descricao"] = "A descrição da categoria é obrigatória"
    } else if (descricao.length > 1000) {
        erros["descricao"] = "A descrição deve ter no máximo 1000 caracteres"
    }

    if (observacao.length > 300) {
        erros["observacao"] = "A observação deve ter no máximo 300 caracteres"
    }

    if (dataCadastro.isNotEmpty() && dataCadastro.toIsoLocalDateOrNull() == null) {
        erros["dataCadastro"] = "Informe uma data válida no formato DD/MM/AAAA"
    }

    return erros
}

fun CategoriaCadastroForm.toCategoriaCadastroDataOrNull(): CategoriaCadastroData? {
    if (validarCategoriaCadastro(this).isNotEmpty()) return null

    val dataCadastro = this.dataCadastro.trim().toIsoLocalDateOrNull()
    return CategoriaCadastroData(
        nome = nome.trim(),
        descricao = descricao.trim(),
        ativo = ativo,
        observacao = observacao.trim().ifEmpty { null },
        dataCadastro = dataCadastro,
    )
}

private fun String.toIsoLocalDateOrNull(): String? {
    if (!matches(Regex("""\d{2}/\d{2}/\d{4}"""))) return null
    val day = substring(0, 2).toInt()
    val month = substring(3, 5).toInt()
    val year = substring(6, 10).toInt()
    if (month !in 1..12) return null
    val daysInMonth = when (month) {
        2 -> if (year.isLeapYear()) 29 else 28
        4, 6, 9, 11 -> 30
        else -> 31
    }
    if (day !in 1..daysInMonth) return null
    return "${year.toString().padStart(4, '0')}-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
}

private fun Int.isLeapYear(): Boolean =
    this % 4 == 0 &&
        (this % 100 != 0 || this % 400 == 0)
