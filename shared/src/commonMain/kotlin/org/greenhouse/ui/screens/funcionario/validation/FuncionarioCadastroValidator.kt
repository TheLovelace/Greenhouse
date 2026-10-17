package org.greenhouse.ui.screens.funcionario.validation

import org.greenhouse.ui.screens.funcionario.model.FuncionarioCadastroData
import org.greenhouse.ui.screens.funcionario.model.FuncionarioCadastroForm

fun validarFuncionarioCadastro(form: FuncionarioCadastroForm): Map<String, String> {
    val errors = mutableMapOf<String, String>()
    val nome = form.nome.trim()
    val cpf = form.cpf.trim()
    val email = form.email.trim()
    val telefone = form.telefone.trim()
    val cargo = form.cargo.trim()

    if (nome.isEmpty()) {
        errors["nome"] = "O nome do funcionário é obrigatório"
    } else if (nome.length > 150) {
        errors["nome"] = "O nome deve ter no máximo 150 caracteres"
    }

    if (!cpf.matches(Regex("""\d{11}"""))) {
        errors["cpf"] = "O CPF deve conter exatamente 11 dígitos numéricos"
    }

    if (email.isEmpty()) {
        errors["email"] = "O e-mail é obrigatório para cadastrar um funcionário"
    } else if (email.length > 150 || !email.matches(Regex("""^[^@\s]+@[^@\s]+\.[^@\s]+$"""))) {
        errors["email"] = "Informe um e-mail válido com até 150 caracteres"
    }

    if (telefone.length > 20) {
        errors["telefone"] = "O telefone deve ter no máximo 20 caracteres"
    }
    if (cargo.length > 100) {
        errors["cargo"] = "O cargo deve ter no máximo 100 caracteres"
    }
    if (form.dataDeNascimento.isNotBlank() && !form.dataDeNascimento.trim().isIsoLocalDate()) {
        errors["dataDeNascimento"] = "Informe uma data válida no formato AAAA-MM-DD"
    }

    return errors
}

fun FuncionarioCadastroForm.toFuncionarioCadastroDataOrNull(): FuncionarioCadastroData? {
    if (validarFuncionarioCadastro(this).isNotEmpty()) return null
    return FuncionarioCadastroData(
        nome = nome.trim(),
        cpf = cpf.trim(),
        telefone = telefone.trim().ifEmpty { null },
        email = email.trim(),
        cargo = cargo.trim().ifEmpty { null },
        ativo = ativo,
        dataDeNascimento = dataDeNascimento.trim().ifEmpty { null },
    )
}

private fun String.isIsoLocalDate(): Boolean {
    if (!matches(Regex("""\d{4}-\d{2}-\d{2}"""))) return false
    val year = substring(0, 4).toInt()
    val month = substring(5, 7).toInt()
    val day = substring(8, 10).toInt()
    if (month !in 1..12) return false
    val daysInMonth = when (month) {
        2 -> if (year.isLeapYear()) 29 else 28
        4, 6, 9, 11 -> 30
        else -> 31
    }
    return day in 1..daysInMonth
}

private fun Int.isLeapYear(): Boolean = this % 4 == 0 && (this % 100 != 0 || this % 400 == 0)
