@file:Suppress("ktlint:standard:function-naming")

package org.greenhouse.ui.screens.alerta

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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import greenhouse.shared.generated.resources.Res
import greenhouse.shared.generated.resources.icon_voltar
import org.greenhouse.app.viewmodel.AlertaViewModel
import org.greenhouse.ui.screens.alerta.model.AlertaCadastroForm
import org.greenhouse.ui.screens.alerta.validation.formatarIdAlerta
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

private val opcoesPrioridade =
    listOf(
        "1" to "Alta (1)",
        "2" to "Média (2)",
        "3" to "Baixa (3)",
    )

@Composable
fun AlertaCadastro(
    viewModel: AlertaViewModel,
    onBack: () -> Unit,
    onSuccess: (String) -> Unit,
    title: String = "Cadastrar Alerta IoT",
    padding: PaddingValues = PaddingValues(),
    embedded: Boolean = false,
) {
    val typography = AppTypography()
    val form = viewModel.formulario()
    val errors = viewModel.erros.value

    fun updateForm(updatedForm: AlertaCadastroForm) {
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
                    text = "Alertas IoT",
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

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                if (maxWidth < 600.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        LabeledTextField(
                            label = "ID do dispositivo IoT",
                            required = true,
                            value = form.dispositivoIotId,
                            onValueChange = { updateForm(form.copy(dispositivoIotId = formatarIdAlerta(it))) },
                            placeholder = "Ex.: 1",
                            error = errors["dispositivoIotId"],
                            keyboardType = KeyboardType.Number,
                        )
                        LabeledTextField(
                            label = "ID da espécie",
                            required = true,
                            value = form.especieId,
                            onValueChange = { updateForm(form.copy(especieId = formatarIdAlerta(it))) },
                            placeholder = "Ex.: 1",
                            error = errors["especieId"],
                            keyboardType = KeyboardType.Number,
                        )
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        LabeledTextField(
                            label = "ID do dispositivo IoT",
                            required = true,
                            value = form.dispositivoIotId,
                            onValueChange = { updateForm(form.copy(dispositivoIotId = formatarIdAlerta(it))) },
                            placeholder = "Ex.: 1",
                            error = errors["dispositivoIotId"],
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(1f),
                        )
                        LabeledTextField(
                            label = "ID da espécie",
                            required = true,
                            value = form.especieId,
                            onValueChange = { updateForm(form.copy(especieId = formatarIdAlerta(it))) },
                            placeholder = "Ex.: 1",
                            error = errors["especieId"],
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            LabeledTextField(
                label = "Título do alerta",
                required = true,
                value = form.titulo,
                onValueChange = { updateForm(form.copy(titulo = it)) },
                placeholder = "Ex.: Temperatura fora do ideal",
                error = errors["titulo"],
                keyboardType = KeyboardType.Text,
            )

            Spacer(Modifier.height(16.dp))

            PrioridadeField(
                prioridade = form.prioridade,
                error = errors["prioridade"],
                onPrioridadeChange = { updateForm(form.copy(prioridade = it)) },
                typography = typography,
            )

            Spacer(Modifier.height(16.dp))

            LabeledTextField(
                label = "Mensagem",
                value = form.mensagem,
                onValueChange = { updateForm(form.copy(mensagem = it)) },
                placeholder = "Digite a mensagem do alerta (opcional)",
                error = errors["mensagem"],
                keyboardType = KeyboardType.Text,
                minLines = 3,
            )

            Spacer(Modifier.height(16.dp))

            RequiredLabel("Condições monitoradas", required = true, typography = typography)
            Spacer(Modifier.height(6.dp))

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                CondicaoCheckbox(
                    label = "Temperatura",
                    checked = form.monitorarTemperatura,
                    onCheckedChange = { updateForm(form.copy(monitorarTemperatura = it)) },
                    typography = typography,
                )
                CondicaoCheckbox(
                    label = "Umidade do ar",
                    checked = form.monitorarUmidadeAr,
                    onCheckedChange = { updateForm(form.copy(monitorarUmidadeAr = it)) },
                    typography = typography,
                )
                CondicaoCheckbox(
                    label = "Umidade do solo",
                    checked = form.monitorarUmidadeSolo,
                    onCheckedChange = { updateForm(form.copy(monitorarUmidadeSolo = it)) },
                    typography = typography,
                )
            }
            FieldError(errors["condicoes"])

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
private fun CondicaoCheckbox(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    typography: androidx.compose.material3.Typography,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(checkedColor = GreenPrimary, uncheckedColor = GreenPrimary),
        )
        Text(text = label, color = TextPrimary, style = typography.bodyMedium)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PrioridadeField(
    prioridade: String,
    error: String?,
    onPrioridadeChange: (String) -> Unit,
    typography: androidx.compose.material3.Typography,
) {
    var expanded by remember { mutableStateOf(false) }
    val rotulo = opcoesPrioridade.firstOrNull { it.first == prioridade }?.second ?: "Selecione a prioridade"

    Column {
        RequiredLabel("Prioridade", required = true, typography = typography)
        Spacer(Modifier.height(6.dp))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
        ) {
            OutlinedTextField(
                value = rotulo,
                onValueChange = {},
                readOnly = true,
                isError = error != null,
                modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                shape = RoundedCornerShape(12.dp),
                colors =
                    androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = Surface,
                        unfocusedContainerColor = Surface,
                        focusedBorderColor = GreenSecondary,
                        unfocusedBorderColor = GreenPrimary,
                        errorBorderColor = RedPrimary,
                    ),
                textStyle = typography.bodyMedium,
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                opcoesPrioridade.forEach { (valor, texto) ->
                    DropdownMenuItem(
                        text = { Text(texto, color = TextPrimary, style = typography.bodyMedium) },
                        onClick = {
                            onPrioridadeChange(valor)
                            expanded = false
                        },
                    )
                }
            }
        }
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
