@file:Suppress("ktlint:standard:function-naming")

package org.greenhouse.ui.screens.especie

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import greenhouse.shared.generated.resources.Res
import greenhouse.shared.generated.resources.icon_voltar
import org.greenhouse.app.viewmodel.EspecieViewModel
import org.greenhouse.ui.screens.especie.model.EspecieCadastroForm
import org.greenhouse.ui.screens.especie.validation.formatarDecimalEspecie
import org.greenhouse.ui.screens.especie.validation.formatarInteiroEspecie
import org.greenhouse.ui.theme.AppTypography
import org.greenhouse.ui.theme.Background
import org.greenhouse.ui.theme.Gray
import org.greenhouse.ui.theme.GreenPrimary
import org.greenhouse.ui.theme.GreenSecondary
import org.greenhouse.ui.theme.RedPrimary
import org.greenhouse.ui.theme.Surface
import org.greenhouse.ui.theme.TextPrimary
import org.greenhouse.ui.theme.White
import org.jetbrains.compose.resources.painterResource

@Composable
fun EspecieCadastro(
    viewModel: EspecieViewModel,
    onBack: () -> Unit,
    onSuccess: (String) -> Unit,
    title: String = "Cadastrar Espécie",
    padding: PaddingValues = PaddingValues(),
    embedded: Boolean = false,
) {
    val typography = AppTypography()
    val form = viewModel.formulario()
    val errors = viewModel.erros.value

    fun updateForm(updatedForm: EspecieCadastroForm) {
        viewModel.atualizarFormulario(updatedForm)
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
    ) {
        if (!embedded) {
            Box(
                modifier = Modifier.fillMaxWidth().height(52.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Espécies",
                    color = TextPrimary,
                    style = typography.titleMedium,
                )
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.align(Alignment.CenterStart).size(48.dp),
                ) {
                    Image(
                        painter = painterResource(Res.drawable.icon_voltar),
                        contentDescription = "Voltar",
                        modifier = Modifier.size(32.dp),
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .widthIn(max = 760.dp)
                    .align(Alignment.CenterHorizontally)
                    .padding(horizontal = 16.dp),
        ) {
            Text(
                text = title,
                color = TextPrimary,
                style = typography.headlineLarge,
            )

            Spacer(Modifier.height(24.dp))

            LabeledTextField(
                label = "Nome popular",
                required = true,
                value = form.nomePopular,
                onValueChange = { updateForm(form.copy(nomePopular = it)) },
                placeholder = "Ex.: Jiboia",
                error = errors["nomePopular"],
                keyboardType = KeyboardType.Text,
            )

            Spacer(Modifier.height(16.dp))

            LabeledTextField(
                label = "Nome científico",
                required = true,
                value = form.nomeCientifico,
                onValueChange = { updateForm(form.copy(nomeCientifico = it)) },
                placeholder = "Ex.: Epipremnum aureum",
                error = errors["nomeCientifico"],
                keyboardType = KeyboardType.Text,
            )

            Spacer(Modifier.height(16.dp))

            LabeledTextField(
                label = "Intervalo de rega (dias)",
                required = true,
                value = form.intervaloRegaDias,
                onValueChange = { updateForm(form.copy(intervaloRegaDias = formatarInteiroEspecie(it))) },
                placeholder = "Ex.: 5",
                error = errors["intervaloRegaDias"],
                keyboardType = KeyboardType.Number,
            )

            Spacer(Modifier.height(16.dp))

            CamposLadoALado(
                primeiro = { modifier ->
                    LabeledTextField(
                        label = "Temperatura mínima (°C)",
                        required = true,
                        value = form.temperaturaMin,
                        onValueChange = { updateForm(form.copy(temperaturaMin = formatarDecimalEspecie(it))) },
                        placeholder = "Ex.: 15,0",
                        error = errors["temperaturaMin"],
                        keyboardType = KeyboardType.Decimal,
                        modifier = modifier,
                    )
                },
                segundo = { modifier ->
                    LabeledTextField(
                        label = "Temperatura máxima (°C)",
                        required = true,
                        value = form.temperaturaMax,
                        onValueChange = { updateForm(form.copy(temperaturaMax = formatarDecimalEspecie(it))) },
                        placeholder = "Ex.: 30,0",
                        error = errors["temperaturaMax"],
                        keyboardType = KeyboardType.Decimal,
                        modifier = modifier,
                    )
                },
            )

            Spacer(Modifier.height(16.dp))

            CamposLadoALado(
                primeiro = { modifier ->
                    LabeledTextField(
                        label = "Umidade do ar mínima (%)",
                        value = form.umidadeArMin,
                        onValueChange = { updateForm(form.copy(umidadeArMin = formatarDecimalEspecie(it))) },
                        placeholder = "Ex.: 40,0",
                        error = errors["umidadeArMin"],
                        keyboardType = KeyboardType.Decimal,
                        modifier = modifier,
                    )
                },
                segundo = { modifier ->
                    LabeledTextField(
                        label = "Umidade do ar máxima (%)",
                        value = form.umidadeArMax,
                        onValueChange = { updateForm(form.copy(umidadeArMax = formatarDecimalEspecie(it))) },
                        placeholder = "Ex.: 70,0",
                        error = errors["umidadeArMax"],
                        keyboardType = KeyboardType.Decimal,
                        modifier = modifier,
                    )
                },
            )

            Spacer(Modifier.height(16.dp))

            CamposLadoALado(
                primeiro = { modifier ->
                    LabeledTextField(
                        label = "Umidade do solo mínima (%)",
                        value = form.umidadeSoloMin,
                        onValueChange = { updateForm(form.copy(umidadeSoloMin = formatarDecimalEspecie(it))) },
                        placeholder = "Ex.: 30,0",
                        error = errors["umidadeSoloMin"],
                        keyboardType = KeyboardType.Decimal,
                        modifier = modifier,
                    )
                },
                segundo = { modifier ->
                    LabeledTextField(
                        label = "Umidade do solo máxima (%)",
                        value = form.umidadeSoloMax,
                        onValueChange = { updateForm(form.copy(umidadeSoloMax = formatarDecimalEspecie(it))) },
                        placeholder = "Ex.: 60,0",
                        error = errors["umidadeSoloMax"],
                        keyboardType = KeyboardType.Decimal,
                        modifier = modifier,
                    )
                },
            )

            Spacer(Modifier.height(16.dp))

            LabeledTextField(
                label = "Descrição",
                value = form.descricao,
                onValueChange = { updateForm(form.copy(descricao = it)) },
                placeholder = "Digite uma descrição (opcional)",
                error = errors["descricao"],
                keyboardType = KeyboardType.Text,
                minLines = 3,
            )

            Spacer(Modifier.height(24.dp))

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                if (maxWidth < 340.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SaveButton(
                            modifier = Modifier.fillMaxWidth(),
                            typography = typography,
                            onClick = { viewModel.gravar(onSuccess) },
                        )
                        ClearButton(
                            modifier = Modifier.fillMaxWidth(),
                            typography = typography,
                            onClick = viewModel::limparCampos,
                        )
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SaveButton(
                            modifier = Modifier.width(160.dp),
                            typography = typography,
                            onClick = { viewModel.gravar(onSuccess) },
                        )
                        ClearButton(
                            modifier = Modifier.width(160.dp),
                            typography = typography,
                            onClick = viewModel::limparCampos,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CamposLadoALado(
    primeiro: @Composable (Modifier) -> Unit,
    segundo: @Composable (Modifier) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        if (maxWidth < 600.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                primeiro(Modifier)
                segundo(Modifier)
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                primeiro(Modifier.weight(1f))
                segundo(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun LabeledTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    error: String?,
    keyboardType: KeyboardType,
    modifier: Modifier = Modifier,
    required: Boolean = false,
    minLines: Int = 1,
) {
    val typography = AppTypography()
    Column(modifier = modifier) {
        RequiredLabel(label, required, typography)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(text = placeholder, color = Gray, style = typography.bodyMedium)
            },
            isError = error != null,
            shape = RoundedCornerShape(12.dp),
            minLines = minLines,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors =
                androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = Surface,
                    unfocusedContainerColor = Surface,
                    focusedBorderColor = GreenSecondary,
                    unfocusedBorderColor = GreenPrimary,
                    errorBorderColor = RedPrimary,
                    cursorColor = GreenPrimary,
                ),
            textStyle = typography.bodyMedium,
            singleLine = minLines == 1,
        )
        FieldError(error)
    }
}

@Composable
private fun FieldError(error: String?) {
    if (error != null) {
        Text(
            text = error,
            color = RedPrimary,
            style = AppTypography().bodySmall,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Composable
private fun RequiredLabel(
    label: String,
    required: Boolean,
    typography: androidx.compose.material3.Typography,
) {
    Row {
        Text(text = label, color = TextPrimary, style = typography.bodyMedium)
        if (required) {
            Text(
                text = " *",
                color = RedPrimary,
                style = typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            )
        }
    }
}

@Composable
private fun SaveButton(
    modifier: Modifier,
    typography: androidx.compose.material3.Typography,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary, contentColor = White),
    ) {
        Text("Gravar", style = typography.bodyMedium)
    }
}

@Composable
private fun ClearButton(
    modifier: Modifier,
    typography: androidx.compose.material3.Typography,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, GreenPrimary),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
    ) {
        Text("Limpar campos", style = typography.bodyMedium)
    }
}
