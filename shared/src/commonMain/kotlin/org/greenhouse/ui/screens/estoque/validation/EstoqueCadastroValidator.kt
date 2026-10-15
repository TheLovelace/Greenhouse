package org.greenhouse.ui.screens.estoque.validation

import org.greenhouse.ui.screens.estoque.model.EstoqueCadastroData
import org.greenhouse.ui.screens.estoque.model.EstoqueCadastroForm

fun formatarQuantidadeEstoque(entrada: String): String = entrada.filter { it in '0'..'9' }

fun validarEstoqueCadastro(form: EstoqueCadastroForm): Map<String, String> {
    val erros = mutableMapOf<String, String>()
    val produtoId = form.produtoId.trim().toLongOrNull()
    val quantidadeFisica = form.quantidadeFisica.trim().ifEmpty { "0" }.toIntOrNull()
    val quantidadeMinima = form.quantidadeMinima.trim().ifEmpty { "0" }.toIntOrNull()
    val localizacao = form.localizacao.trim()

    if (form.produtoId.isBlank()) {
        erros["produtoId"] = "O ID do produto é obrigatório"
    } else if (produtoId == null || produtoId <= 0) {
        erros["produtoId"] = "Informe um ID de produto maior que zero"
    }

    if (quantidadeFisica == null) {
        erros["quantidadeFisica"] = "Informe uma quantidade física válida"
    } else if (quantidadeFisica < 0) {
        erros["quantidadeFisica"] = "A quantidade física não pode ser negativa"
    }

    if (quantidadeMinima == null) {
        erros["quantidadeMinima"] = "Informe uma quantidade mínima válida"
    } else if (quantidadeMinima < 0) {
        erros["quantidadeMinima"] = "A quantidade mínima não pode ser negativa"
    }

    if (localizacao.length > 100) {
        erros["localizacao"] = "A localização deve ter no máximo 100 caracteres"
    }

    return erros
}

fun EstoqueCadastroForm.toEstoqueCadastroDataOrNull(): EstoqueCadastroData? {
    if (validarEstoqueCadastro(this).isNotEmpty()) return null

    return EstoqueCadastroData(
        produtoId = requireNotNull(produtoId.trim().toLongOrNull()),
        quantidadeFisica = quantidadeFisica.trim().ifEmpty { "0" }.toInt(),
        quantidadeMinima = quantidadeMinima.trim().ifEmpty { "0" }.toInt(),
        localizacao = localizacao.trim().ifEmpty { null },
        bloqueado = bloqueado,
    )
}
