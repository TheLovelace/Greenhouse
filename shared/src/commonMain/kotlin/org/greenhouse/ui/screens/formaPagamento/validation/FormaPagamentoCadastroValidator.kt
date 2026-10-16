package org.greenhouse.ui.screens.formaPagamento.validation

import org.greenhouse.ui.screens.formaPagamento.model.FormaPagamentoCadastroData
import org.greenhouse.ui.screens.formaPagamento.model.FormaPagamentoCadastroForm

fun formatarNumeroFormaPagamento(value: String): String = value.filter { it in '0'..'9' }

fun validarFormaPagamentoCadastro(form: FormaPagamentoCadastroForm): Map<String, String> {
    val errors = mutableMapOf<String, String>()
    val nome = form.nome.trim()
    val descricao = form.descricao.trim()
    val limiteParcelas = form.limiteParcelas.trim().toIntOrNull()
    val prazoEstornoDias = form.prazoEstornoDias.trim().toIntOrNull()

    if (nome.isEmpty()) {
        errors["nome"] = "O nome da forma de pagamento é obrigatório"
    } else if (nome.length > 50) {
        errors["nome"] = "O nome deve ter no máximo 50 caracteres"
    }
    if (descricao.length > 250) {
        errors["descricao"] = "A descrição deve ter no máximo 250 caracteres"
    }
    if (limiteParcelas == null || limiteParcelas < 1) {
        errors["limiteParcelas"] = "O limite de parcelas deve ser um número maior ou igual a 1"
    }
    if (form.prazoEstornoDias.isNotBlank() && (prazoEstornoDias == null || prazoEstornoDias < 0)) {
        errors["prazoEstornoDias"] = "O prazo de estorno não pode ser negativo"
    }

    return errors
}

fun FormaPagamentoCadastroForm.toFormaPagamentoCadastroDataOrNull(): FormaPagamentoCadastroData? {
    if (validarFormaPagamentoCadastro(this).isNotEmpty()) return null
    return FormaPagamentoCadastroData(
        nome = nome.trim(),
        ativo = ativo,
        descricao = descricao.trim().ifEmpty { null },
        limiteParcelas = requireNotNull(limiteParcelas.trim().toIntOrNull()),
        prazoEstornoDias = prazoEstornoDias.trim().takeIf { it.isNotEmpty() }?.toInt(),
    )
}
