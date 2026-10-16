package org.greenhouse.ui.screens.categoria.validation

import org.greenhouse.ui.screens.categoria.model.CategoriaCadastroData
import org.greenhouse.ui.screens.categoria.model.CategoriaCadastroForm

fun validarCategoriaCadastro(form: CategoriaCadastroForm): Map<String, String> {
    val erros = mutableMapOf<String, String>()
    val nome = form.nome.trim()
    val descricao = form.descricao.trim()
    val observacao = form.observacao.trim()

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

    return erros
}

fun CategoriaCadastroForm.toCategoriaCadastroDataOrNull(): CategoriaCadastroData? {
    if (validarCategoriaCadastro(this).isNotEmpty()) return null

    return CategoriaCadastroData(
        nome = nome.trim(),
        descricao = descricao.trim(),
        ativo = ativo,
        observacao = observacao.trim().ifEmpty { null },
    )
}
