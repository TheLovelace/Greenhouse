package org.greenhouse.ui.screens.produto.validation

import org.greenhouse.ui.screens.produto.model.ProdutoCadastroData
import org.greenhouse.ui.screens.produto.model.ProdutoCadastroForm

fun formatarPrecoProduto(entrada: String): String {
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

fun formatarPrazoPreparoProduto(entrada: String): String = entrada.filter { it in '0'..'9' }

fun validarProdutoCadastro(form: ProdutoCadastroForm): Map<String, String> {
    val erros = mutableMapOf<String, String>()
    val codigo = form.codigo.trim()
    val nome = form.nome.trim()
    val descricao = form.descricao.trim()
    val preco = form.preco.trim().replace(',', '.')
    val prazo = form.prazoPreparoMinutos.trim()

    if (codigo.length > 50) {
        erros["codigo"] = "O código deve ter no máximo 50 caracteres"
    }

    if (nome.isEmpty()) {
        erros["nome"] = "O nome do produto é obrigatório"
    } else if (nome.length > 150) {
        erros["nome"] = "O nome deve ter no máximo 150 caracteres"
    }

    if (descricao.length > 1000) {
        erros["descricao"] = "A descrição deve ter no máximo 1000 caracteres"
    }

    if (form.preco.isBlank()) {
        erros["preco"] = "O preço do produto é obrigatório"
    } else if (!preco.matches(Regex("""\d+(\.\d{1,2})?"""))) {
        erros["preco"] = "Informe um preço válido com até 2 casas decimais"
    } else if (preco.compareToDecimalProduto("9999999999.99") > 0) {
        erros["preco"] = "O preço deve ter no máximo 10 dígitos inteiros"
    }

    if (prazo.isNotEmpty() && (prazo.toIntOrNull() == null || prazo.toInt() < 0)) {
        erros["prazoPreparoMinutos"] = "Informe um prazo válido em minutos"
    }

    return erros
}

fun ProdutoCadastroForm.toProdutoCadastroDataOrNull(): ProdutoCadastroData? {
    if (validarProdutoCadastro(this).isNotEmpty()) return null

    return ProdutoCadastroData(
        codigo = codigo.trim().ifEmpty { null },
        nome = nome.trim(),
        descricao = descricao.trim().ifEmpty { null },
        preco = preco.trim().replace(',', '.'),
        ativo = ativo,
        prazoPreparoMinutos = prazoPreparoMinutos.trim().toIntOrNull(),
    )
}

private fun String.compareToDecimalProduto(other: String): Int {
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
