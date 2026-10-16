package org.greenhouse.ui.screens.cliente.validation

import org.greenhouse.ui.screens.cliente.model.ClienteCadastroData
import org.greenhouse.ui.screens.cliente.model.ClienteCadastroForm

fun formatarCpf(entrada: String): String = entrada.filter(Char::isDigit).take(11)

fun validarClienteCadastro(form: ClienteCadastroForm): Map<String, String> {
    val erros = mutableMapOf<String, String>()
    val nome = form.nome.trim()
    val telefone = form.telefone.trim()
    val cpf = form.cpf.trim()
    val email = form.email.trim()

    if (nome.isEmpty()) {
        erros["nome"] = "O nome do cliente é obrigatório"
    } else if (nome.length > 150) {
        erros["nome"] = "O nome deve ter no máximo 150 caracteres"
    }

    if (telefone.isEmpty()) {
        erros["telefone"] = "O telefone do cliente é obrigatório"
    } else if (telefone.length > 20) {
        erros["telefone"] = "O telefone deve ter no máximo 20 caracteres"
    }

    if (cpf.isNotEmpty() && !cpf.matches(Regex("""\d{11}"""))) {
        erros["cpf"] = "O CPF deve conter 11 dígitos numéricos"
    }

    if (email.isNotEmpty()) {
        if (email.length > 150) {
            erros["email"] = "O e-mail deve ter no máximo 150 caracteres"
        } else if (!email.matches(Regex("""[^@\s]+@[^@\s]+\.[^@\s]+"""))) {
            erros["email"] = "Informe um e-mail válido"
        }
    }

    return erros
}

fun ClienteCadastroForm.toClienteCadastroDataOrNull(): ClienteCadastroData? {
    if (validarClienteCadastro(this).isNotEmpty()) return null

    return ClienteCadastroData(
        nome = nome.trim(),
        telefone = telefone.trim(),
        whatsapp = whatsapp,
        cpf = cpf.trim().ifEmpty { null },
        email = email.trim().ifEmpty { null },
        ativo = ativo,
    )
}
