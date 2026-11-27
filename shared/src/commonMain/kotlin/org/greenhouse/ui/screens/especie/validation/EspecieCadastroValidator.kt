package org.greenhouse.ui.screens.especie.validation

import org.greenhouse.ui.screens.especie.model.EspecieCadastroData
import org.greenhouse.ui.screens.especie.model.EspecieCadastroForm

private const val LIMITE_TEMPERATURA = 999.99
private val padraoDecimal = Regex("""\d+([.,]\d{1,2})?""")

fun formatarInteiroEspecie(texto: String): String = texto.filter(Char::isDigit)

/** Mantém apenas dígitos, um separador decimal (. ou ,) e no máximo 2 casas decimais. */
fun formatarDecimalEspecie(texto: String): String {
    val resultado = StringBuilder()
    var separadorEncontrado = false
    var casasDecimais = 0

    for (caractere in texto) {
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

private fun String.toDecimalOrNull(): Double? {
    val valor = trim()
    if (!valor.matches(padraoDecimal)) return null
    return valor.replace(',', '.').toDoubleOrNull()
}

private fun String.decimalInvalido(): Boolean = isNotBlank() && toDecimalOrNull() == null

fun validarEspecieCadastro(form: EspecieCadastroForm): Map<String, String> {
    val erros = mutableMapOf<String, String>()
    val nomePopular = form.nomePopular.trim()
    val nomeCientifico = form.nomeCientifico.trim()
    val descricao = form.descricao.trim()
    val rega = form.intervaloRegaDias.trim().toIntOrNull()
    val tempMin = form.temperaturaMin.toDecimalOrNull()
    val tempMax = form.temperaturaMax.toDecimalOrNull()
    val arMin = form.umidadeArMin.toDecimalOrNull()
    val arMax = form.umidadeArMax.toDecimalOrNull()
    val soloMin = form.umidadeSoloMin.toDecimalOrNull()
    val soloMax = form.umidadeSoloMax.toDecimalOrNull()

    if (nomePopular.isEmpty()) {
        erros["nomePopular"] = "O nome popular é obrigatório"
    } else if (nomePopular.length > 150) {
        erros["nomePopular"] = "O nome popular deve ter no máximo 150 caracteres"
    }

    if (nomeCientifico.isEmpty()) {
        erros["nomeCientifico"] = "O nome científico é obrigatório"
    } else if (nomeCientifico.length > 150) {
        erros["nomeCientifico"] = "O nome científico deve ter no máximo 150 caracteres"
    }

    if (form.intervaloRegaDias.isBlank()) {
        erros["intervaloRegaDias"] = "O intervalo de rega é obrigatório"
    } else if (rega == null || rega <= 0) {
        erros["intervaloRegaDias"] = "O intervalo de rega deve ser um número inteiro maior que zero"
    }

    if (form.temperaturaMin.isBlank()) {
        erros["temperaturaMin"] = "A temperatura mínima é obrigatória"
    } else if (tempMin == null) {
        erros["temperaturaMin"] = "Informe um valor numérico válido com até 2 casas decimais"
    } else if (tempMin > LIMITE_TEMPERATURA) {
        erros["temperaturaMin"] = "A temperatura deve ter no máximo 3 dígitos inteiros"
    }

    if (form.temperaturaMax.isBlank()) {
        erros["temperaturaMax"] = "A temperatura máxima é obrigatória"
    } else if (tempMax == null) {
        erros["temperaturaMax"] = "Informe um valor numérico válido com até 2 casas decimais"
    } else if (tempMax > LIMITE_TEMPERATURA) {
        erros["temperaturaMax"] = "A temperatura deve ter no máximo 3 dígitos inteiros"
    }

    if (tempMin != null && tempMax != null && tempMin > tempMax && !erros.containsKey("temperaturaMax")) {
        erros["temperaturaMax"] = "A temperatura máxima deve ser maior ou igual à mínima"
    }

    validarUmidade(form.umidadeArMin, arMin, "umidadeArMin", erros)
    validarUmidade(form.umidadeArMax, arMax, "umidadeArMax", erros)
    if (arMin != null && arMax != null && arMin > arMax && !erros.containsKey("umidadeArMax")) {
        erros["umidadeArMax"] = "A umidade máxima deve ser maior ou igual à mínima"
    }

    validarUmidade(form.umidadeSoloMin, soloMin, "umidadeSoloMin", erros)
    validarUmidade(form.umidadeSoloMax, soloMax, "umidadeSoloMax", erros)
    if (soloMin != null && soloMax != null && soloMin > soloMax && !erros.containsKey("umidadeSoloMax")) {
        erros["umidadeSoloMax"] = "A umidade máxima deve ser maior ou igual à mínima"
    }

    if (descricao.length > 2000) {
        erros["descricao"] = "A descrição deve ter no máximo 2000 caracteres"
    }

    return erros
}

private fun validarUmidade(
    texto: String,
    valor: Double?,
    campo: String,
    erros: MutableMap<String, String>,
) {
    if (texto.isBlank()) return
    if (texto.decimalInvalido()) {
        erros[campo] = "Informe um valor numérico válido com até 2 casas decimais"
    } else if (valor != null && valor !in 0.0..100.0) {
        erros[campo] = "A umidade deve estar entre 0 e 100"
    }
}

fun EspecieCadastroForm.toEspecieCadastroDataOrNull(): EspecieCadastroData? {
    if (validarEspecieCadastro(this).isNotEmpty()) return null
    return EspecieCadastroData(
        nomePopular = nomePopular.trim(),
        nomeCientifico = nomeCientifico.trim(),
        descricao = descricao.trim().ifEmpty { null },
        intervaloRegaDias = intervaloRegaDias.trim().toInt(),
        temperaturaMin = requireNotNull(temperaturaMin.toDecimalOrNull()),
        temperaturaMax = requireNotNull(temperaturaMax.toDecimalOrNull()),
        umidadeArMin = umidadeArMin.toDecimalOrNull(),
        umidadeArMax = umidadeArMax.toDecimalOrNull(),
        umidadeSoloMin = umidadeSoloMin.toDecimalOrNull(),
        umidadeSoloMax = umidadeSoloMax.toDecimalOrNull(),
    )
}
