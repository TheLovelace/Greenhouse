@file:Suppress("ktlint:standard:function-naming")

package org.greenhouse.ui.screens.desconto

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import org.greenhouse.ui.screens.desconto.model.DescontoCadastroData
import org.greenhouse.ui.screens.desconto.model.DescontoCadastroForm
import org.greenhouse.ui.screens.desconto.model.TipoDescontoCadastro
import org.greenhouse.ui.screens.desconto.validation.formatarDataBrasileira
import org.greenhouse.ui.screens.desconto.validation.formatarValorDesconto
import org.greenhouse.ui.screens.desconto.validation.toOffsetDateTimeOrNull
import org.greenhouse.ui.screens.desconto.validation.validarDescontoCadastro
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
fun DescontoCadastro(
    onBack: () -> Unit,
    onSave: (DescontoCadastroData) -> Unit,
) {
    val typography = AppTypography()
    var form by remember { mutableStateOf(DescontoCadastroForm()) }
    var errors by remember { mutableStateOf(emptyMap<String, String>()) }

    fun updateForm(updatedForm: DescontoCadastroForm) {
        form = updatedForm
        if (errors.isNotEmpty()) {
            errors = validarDescontoCadastro(updatedForm)
        }
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().height(52.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Descontos",
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

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .widthIn(max = 760.dp)
                    .align(Alignment.CenterHorizontally)
                    .padding(horizontal = 16.dp),
        ) {
            Text(
                text = "Cadastrar Desconto",
                color = TextPrimary,
                style = typography.headlineLarge,
            )

            Spacer(Modifier.height(24.dp))

            LabeledTextField(
                label = "Título",
                required = true,
                value = form.titulo,
                onValueChange = { updateForm(form.copy(titulo = it)) },
                placeholder = "Digite o título do desconto",
                error = errors["titulo"],
                keyboardType = KeyboardType.Text,
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

            Spacer(Modifier.height(16.dp))

            TipoDescontoField(
                tipo = form.tipo,
                onTipoChange = { updateForm(form.copy(tipo = it)) },
                typography = typography,
            )

            Spacer(Modifier.height(16.dp))

            LabeledTextField(
                label = "Valor",
                required = true,
                value = form.valor,
                onValueChange = { value ->
                    updateForm(form.copy(valor = formatarValorDesconto(value)))
                },
                placeholder = if (form.tipo == TipoDescontoCadastro.PERCENTUAL) "Ex.: 10" else "Ex.: 25,50",
                error = errors["valor"],
                keyboardType = KeyboardType.Decimal,
            )

            Spacer(Modifier.height(16.dp))

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                if (maxWidth < 600.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        DateInputField(
                            label = "Data inicial",
                            value = form.dataInicio,
                            error = errors["dataInicio"],
                            onValueChange = { value ->
                                updateForm(form.copy(dataInicio = formatarDataBrasileira(value)))
                            },
                        )
                        DateInputField(
                            label = "Data final",
                            value = form.dataFim,
                            error = errors["dataFim"],
                            onValueChange = { value ->
                                updateForm(form.copy(dataFim = formatarDataBrasileira(value)))
                            },
                        )
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        DateInputField(
                            label = "Data inicial",
                            value = form.dataInicio,
                            error = errors["dataInicio"],
                            onValueChange = { value ->
                                updateForm(form.copy(dataInicio = formatarDataBrasileira(value)))
                            },
                            modifier = Modifier.weight(1f),
                        )
                        DateInputField(
                            label = "Data final",
                            value = form.dataFim,
                            error = errors["dataFim"],
                            onValueChange = { value ->
                                updateForm(form.copy(dataFim = formatarDataBrasileira(value)))
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            LabeledTextField(
                label = "Quantidade mínima de itens",
                required = true,
                value = form.quantidadeMinimaItens,
                onValueChange = { value ->
                    if (value.all(Char::isDigit)) {
                        updateForm(form.copy(quantidadeMinimaItens = value))
                    }
                },
                placeholder = "Informe a quantidade mínima",
                error = errors["quantidadeMinimaItens"],
                keyboardType = KeyboardType.Number,
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = form.ativo,
                    onCheckedChange = { updateForm(form.copy(ativo = it)) },
                    colors = CheckboxDefaults.colors(checkedColor = GreenPrimary, uncheckedColor = GreenPrimary),
                )
                Text("Deixar o desconto ativo?", color = TextPrimary, style = typography.bodyMedium)
            }

            Spacer(Modifier.height(24.dp))

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                if (maxWidth < 320.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SaveButton(
                            modifier = Modifier.width(160.dp),
                            typography = typography,
                            onClick = { saveForm(form, { data -> onSave(data) }) { errors = it } },
                        )
                        ClearButton(
                            modifier = Modifier.width(160.dp),
                            typography = typography,
                            onClick = {
                                form = DescontoCadastroForm()
                                errors = emptyMap()
                            },
                        )
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SaveButton(
                            modifier = Modifier.width(160.dp),
                            typography = typography,
                            onClick = { saveForm(form, { data -> onSave(data) }) { errors = it } },
                        )
                        ClearButton(
                            modifier = Modifier.width(160.dp),
                            typography = typography,
                            onClick = {
                                form = DescontoCadastroForm()
                                errors = emptyMap()
                            },
                        )
                    }
                }
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
private fun DateInputField(
    label: String,
    value: String,
    error: String?,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LabeledTextField(
        label = label,
        value = value,
        onValueChange = onValueChange,
        placeholder = "DD/MM/AAAA",
        error = error,
        keyboardType = KeyboardType.Number,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TipoDescontoField(
    tipo: TipoDescontoCadastro,
    onTipoChange: (TipoDescontoCadastro) -> Unit,
    typography: androidx.compose.material3.Typography,
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        RequiredLabel("Tipo de desconto", required = true, typography = typography)
        Spacer(Modifier.height(6.dp))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
        ) {
            OutlinedTextField(
                value = tipo.label(),
                onValueChange = {},
                readOnly = true,
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
                    ),
                textStyle = typography.bodyMedium,
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                TipoDescontoCadastro.entries.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.label(), color = TextPrimary, style = typography.bodyMedium) },
                        onClick = {
                            onTipoChange(option)
                            expanded = false
                        },
                    )
                }
            }
        }
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

private fun saveForm(
    form: DescontoCadastroForm,
    onSave: (DescontoCadastroData) -> Unit,
    onValidationErrors: (Map<String, String>) -> Unit,
) {
    val validationErrors = validarDescontoCadastro(form)
    onValidationErrors(validationErrors)
    if (validationErrors.isEmpty()) {
        onSave(
            DescontoCadastroData(
                titulo = form.titulo.trim(),
                descricao = form.descricao.trim().ifEmpty { null },
                tipo = form.tipo,
                valor = form.valor.trim().replace(',', '.'),
                dataInicio = form.dataInicio.toOffsetDateTimeOrNull(),
                dataFim = form.dataFim.toOffsetDateTimeOrNull(),
                ativo = form.ativo,
                quantidadeMinimaItens = form.quantidadeMinimaItens.trim(),
            ),
        )
    }
}

private fun TipoDescontoCadastro.label(): String =
    when (this) {
        TipoDescontoCadastro.PERCENTUAL -> "Percentual"
        TipoDescontoCadastro.VALOR_FIXO -> "Valor fixo"
    }
