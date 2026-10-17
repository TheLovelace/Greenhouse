package org.greenhouse.ui.screens.fornecedor.validation

import org.greenhouse.ui.screens.fornecedor.model.FornecedorCadastroData
import org.greenhouse.ui.screens.fornecedor.model.FornecedorCadastroForm

fun formatarCnpj(entrada: String): String = entrada.filter(Char::isDigit).take(14)

fun formatarDigitosFornecedor(entrada: String): String = entrada.filter(Char::isDigit)

fun validarFornecedorCadastro(form: FornecedorCadastroForm): Map<String, String> {
    val erros = mutableMapOf<String, String>()
    val nome = form.nome.trim()
    val cnpj = form.cnpj.trim()
    val telefone = form.telefone.trim()
    val email = form.email.trim()
    val contato = form.contato.trim()
    val prazoTexto = form.prazoMedioEntregaDias.trim()

    if (nome.isEmpty()) {
        erros["nome"] = "O nome do fornecedor é obrigatório"
    } else if (nome.length > 150) {
        erros["nome"] = "O nome deve ter no máximo 150 caracteres"
    }

    if (cnpj.isNotEmpty() && !cnpj.matches(Regex("""\d{14}"""))) {
        erros["cnpj"] = "O CNPJ deve conter exatamente 14 dígitos numéricos"
    }

    if (telefone.length > 20) {
        erros["telefone"] = "O telefone deve ter no máximo 20 caracteres"
    }

    if (email.isNotEmpty()) {
        if (email.length > 150) {
            erros["email"] = "O e-mail deve ter no máximo 150 caracteres"
        } else if (!email.matches(Regex("""[^@\s]+@[^@\s]+\.[^@\s]+"""))) {
            erros["email"] = "Informe um e-mail válido"
        }
    }

    if (contato.length > 150) {
        erros["contato"] = "O contato deve ter no máximo 150 caracteres"
    }

    if (prazoTexto.isNotEmpty()) {
        val prazo = prazoTexto.toIntOrNull()
        if (prazo == null) {
            erros["prazoMedioEntregaDias"] = "Informe um prazo médio de entrega válido"
        }
    }

    return erros
}

fun FornecedorCadastroForm.toFornecedorCadastroDataOrNull(): FornecedorCadastroData? {
    if (validarFornecedorCadastro(this).isNotEmpty()) return null

    return FornecedorCadastroData(
        nome = nome.trim(),
        cnpj = cnpj.trim().ifEmpty { null },
        telefone = telefone.trim().ifEmpty { null },
        email = email.trim().ifEmpty { null },
        contato = contato.trim().ifEmpty { null },
        ativo = ativo,
        prazoMedioEntregaDias = prazoMedioEntregaDias.trim().ifEmpty { null }?.toIntOrNull(),
    )
}
