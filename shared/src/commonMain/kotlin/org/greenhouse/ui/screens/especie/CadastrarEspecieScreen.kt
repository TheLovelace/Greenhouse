package org.greenhouse.ui.screens.especie

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.greenhouse.ui.theme.AppTypography
import org.greenhouse.ui.theme.Background
import org.greenhouse.ui.theme.GreenPrimary
import org.greenhouse.ui.theme.RedPrimary
import org.greenhouse.ui.theme.Surface
import org.greenhouse.ui.theme.TextPrimary

/** Campos do formulário, espelhando o EspecieRequestDTO do backend. */
data class EspecieFormulario(
    val nomePopular: String,
    val nomeCientifico: String,
    val descricao: String?,
    val intervaloRegaDias: Int,
    val temperaturaMin: Double,
    val temperaturaMax: Double,
    val umidadeArMin: Double?,
    val umidadeArMax: Double?,
    val umidadeSoloMin: Double?,
    val umidadeSoloMax: Double?,
)

@Composable
fun CadastrarEspecieScreen(
    onBackClick: () -> Unit = {},
    onSalvarClick: (EspecieFormulario) -> Unit = {},
) {
    var nomePopular by remember { mutableStateOf("") }
    var nomeCientifico by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }
    var intervaloRega by remember { mutableStateOf("") }
    var temperaturaMinima by remember { mutableStateOf("") }
    var temperaturaMaxima by remember { mutableStateOf("") }
    var umidadeArMinima by remember { mutableStateOf("") }
    var umidadeArMaxima by remember { mutableStateOf("") }
    var umidadeSoloMinima by remember { mutableStateOf("") }
    var umidadeSoloMaxima by remember { mutableStateOf("") }

    var erro by remember { mutableStateOf("") }
    var dadosGravados by remember { mutableStateOf<EspecieFormulario?>(null) }

    val typography = AppTypography()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(scrollState)
            .padding(horizontal = 30.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onBackClick) {
                Text(
                    text = "← Voltar",
                    color = GreenPrimary,
                    style = typography.titleMedium,
                )
            }
        }

        Text(
            text = "Cadastrar Espécie",
            style = typography.headlineMedium,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold,
        )

        Text(
            text = "Preencha os dados para cadastrar uma nova espécie de planta.",
            style = typography.bodyLarge,
            color = TextPrimary,
        )

        CampoFormularioGreenhouse(
            label = "Nome popular *",
            value = nomePopular,
            onValueChange = {
                nomePopular = it
                erro = ""
            },
            placeholder = "Ex.: Jiboia",
            typography = typography,
        )

        CampoFormularioGreenhouse(
            label = "Nome científico *",
            value = nomeCientifico,
            onValueChange = {
                nomeCientifico = it
                erro = ""
            },
            placeholder = "Ex.: Epipremnum aureum",
            typography = typography,
        )

        CampoFormularioGreenhouse(
            label = "Intervalo de rega (dias) *",
            value = intervaloRega,
            onValueChange = {
                intervaloRega = it
                erro = ""
            },
            placeholder = "5",
            keyboardType = KeyboardType.Number,
            typography = typography,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                CampoFormularioGreenhouse(
                    label = "Temp. mín. (°C) *",
                    value = temperaturaMinima,
                    onValueChange = {
                        temperaturaMinima = it
                        erro = ""
                    },
                    placeholder = "15.0",
                    keyboardType = KeyboardType.Decimal,
                    typography = typography,
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                CampoFormularioGreenhouse(
                    label = "Temp. máx. (°C) *",
                    value = temperaturaMaxima,
                    onValueChange = {
                        temperaturaMaxima = it
                        erro = ""
                    },
                    placeholder = "30.0",
                    keyboardType = KeyboardType.Decimal,
                    typography = typography,
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                CampoFormularioGreenhouse(
                    label = "Umidade do ar mín. (%)",
                    value = umidadeArMinima,
                    onValueChange = {
                        umidadeArMinima = it
                        erro = ""
                    },
                    placeholder = "40.0",
                    keyboardType = KeyboardType.Decimal,
                    typography = typography,
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                CampoFormularioGreenhouse(
                    label = "Umidade do ar máx. (%)",
                    value = umidadeArMaxima,
                    onValueChange = {
                        umidadeArMaxima = it
                        erro = ""
                    },
                    placeholder = "70.0",
                    keyboardType = KeyboardType.Decimal,
                    typography = typography,
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                CampoFormularioGreenhouse(
                    label = "Umidade do solo mín. (%)",
                    value = umidadeSoloMinima,
                    onValueChange = {
                        umidadeSoloMinima = it
                        erro = ""
                    },
                    placeholder = "30.0",
                    keyboardType = KeyboardType.Decimal,
                    typography = typography,
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                CampoFormularioGreenhouse(
                    label = "Umidade do solo máx. (%)",
                    value = umidadeSoloMaxima,
                    onValueChange = {
                        umidadeSoloMaxima = it
                        erro = ""
                    },
                    placeholder = "60.0",
                    keyboardType = KeyboardType.Decimal,
                    typography = typography,
                )
            }
        }

        CampoFormularioGreenhouse(
            label = "Descrição",
            value = descricao,
            onValueChange = {
                descricao = it
                erro = ""
            },
            placeholder = "Ex.: Planta de meia-sombra, fácil cultivo...",
            singleLine = false,
            typography = typography,
        )

        if (erro.isNotBlank()) {
            Text(
                text = erro,
                style = typography.bodyMedium,
                color = RedPrimary,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                onClick = {
                    val rega = intervaloRega.trim().toIntOrNull()
                    val tempMin = temperaturaMinima.paraDecimal()
                    val tempMax = temperaturaMaxima.paraDecimal()
                    val arMin = umidadeArMinima.paraDecimal()
                    val arMax = umidadeArMaxima.paraDecimal()
                    val soloMin = umidadeSoloMinima.paraDecimal()
                    val soloMax = umidadeSoloMaxima.paraDecimal()

                    erro = when {
                        nomePopular.isBlank() ->
                            "Informe o nome popular."

                        nomePopular.trim().length > 150 ->
                            "O nome popular deve ter no máximo 150 caracteres."

                        nomeCientifico.isBlank() ->
                            "Informe o nome científico."

                        nomeCientifico.trim().length > 150 ->
                            "O nome científico deve ter no máximo 150 caracteres."

                        rega == null || rega < 1 ->
                            "Informe o intervalo de rega em dias (número inteiro maior que zero)."

                        tempMin == null ->
                            "Informe a temperatura mínima."

                        tempMax == null ->
                            "Informe a temperatura máxima."

                        tempMin > tempMax ->
                            "A temperatura mínima não pode ser maior que a máxima."

                        umidadeInvalida(umidadeArMinima, arMin) ->
                            "A umidade do ar mínima deve estar entre 0 e 100."

                        umidadeInvalida(umidadeArMaxima, arMax) ->
                            "A umidade do ar máxima deve estar entre 0 e 100."

                        arMin != null && arMax != null && arMin > arMax ->
                            "A umidade do ar mínima não pode ser maior que a máxima."

                        umidadeInvalida(umidadeSoloMinima, soloMin) ->
                            "A umidade do solo mínima deve estar entre 0 e 100."

                        umidadeInvalida(umidadeSoloMaxima, soloMax) ->
                            "A umidade do solo máxima deve estar entre 0 e 100."

                        soloMin != null && soloMax != null && soloMin > soloMax ->
                            "A umidade do solo mínima não pode ser maior que a máxima."

                        descricao.trim().length > 2000 ->
                            "A descrição deve ter no máximo 2000 caracteres."

                        else -> ""
                    }

                    if (erro.isBlank()) {
                        dadosGravados = EspecieFormulario(
                            nomePopular = nomePopular.trim(),
                            nomeCientifico = nomeCientifico.trim(),
                            descricao = descricao.trim().ifBlank { null },
                            intervaloRegaDias = rega!!,
                            temperaturaMin = tempMin!!,
                            temperaturaMax = tempMax!!,
                            umidadeArMin = arMin,
                            umidadeArMax = arMax,
                            umidadeSoloMin = soloMin,
                            umidadeSoloMax = soloMax,
                        )
                    }
                },
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GreenPrimary,
                    contentColor = Surface,
                ),
                contentPadding = PaddingValues(horizontal = 12.dp),
            ) {
                Text(
                    text = "Gravar",
                    style = typography.titleMedium,
                )
            }

            OutlinedButton(
                onClick = {
                    nomePopular = ""
                    nomeCientifico = ""
                    descricao = ""
                    intervaloRega = ""
                    temperaturaMinima = ""
                    temperaturaMaxima = ""
                    umidadeArMinima = ""
                    umidadeArMaxima = ""
                    umidadeSoloMinima = ""
                    umidadeSoloMaxima = ""
                    erro = ""
                },
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, GreenPrimary),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = TextPrimary,
                ),
                contentPadding = PaddingValues(horizontal = 12.dp),
            ) {
                Text(
                    text = "Limpar campos",
                    style = typography.titleMedium,
                )
            }
        }
    }

    dadosGravados?.let { dados ->
        val fechar = {
            dadosGravados = null
            onSalvarClick(dados)
        }

        AlertDialog(
            onDismissRequest = fechar,
            containerColor = Background,
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(
                    text = "Cadastro concluído",
                    style = typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                )
            },
            text = {
                Text(
                    text = "Espécie cadastrada com sucesso!",
                    style = typography.bodyLarge,
                    color = TextPrimary,
                )
            },
            confirmButton = {
                Button(
                    onClick = fechar,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GreenPrimary,
                        contentColor = Surface,
                    ),
                ) {
                    Text(
                        text = "OK",
                        style = typography.titleMedium,
                    )
                }
            },
        )
    }
}

private fun String.paraDecimal(): Double? = trim().replace(',', '.').toDoubleOrNull()

/** Campo opcional de umidade: vazio é aceito; preenchido precisa ser número entre 0 e 100. */
private fun umidadeInvalida(texto: String, valor: Double?): Boolean =
    texto.isNotBlank() && (valor == null || valor !in 0.0..100.0)

@Composable
fun CampoFormularioGreenhouse(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    typography: androidx.compose.material3.Typography,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = typography.titleSmall,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = 6.dp),
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = placeholder,
                    style = typography.bodyMedium,
                )
            },
            singleLine = singleLine,
            minLines = if (singleLine) 1 else 3,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Surface,
                unfocusedContainerColor = Surface,
                focusedBorderColor = GreenPrimary,
                unfocusedBorderColor = GreenPrimary,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedPlaceholderColor = TextPrimary.copy(alpha = 0.5f),
                unfocusedPlaceholderColor = TextPrimary.copy(alpha = 0.5f),
            ),
        )
    }
}