@file:Suppress("ktlint:standard:function-naming")

package org.greenhouse.ui.screens.formaPagamento

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
import org.greenhouse.ui.screens.formaPagamento.model.FormaPagamentoCadastroData
import org.greenhouse.ui.screens.formaPagamento.model.FormaPagamentoCadastroForm
import org.greenhouse.ui.screens.formaPagamento.validation.formatarNumeroFormaPagamento
import org.greenhouse.ui.screens.formaPagamento.validation.toFormaPagamentoCadastroDataOrNull
import org.greenhouse.ui.screens.formaPagamento.validation.validarFormaPagamentoCadastro
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
fun FormaPagamentoCadastro(
    onBack: () -> Unit,
    onSave: (FormaPagamentoCadastroData) -> Unit,
    initialData: FormaPagamentoCadastroData? = null,
    recordId: Long? = null,
    isSaving: Boolean = false,
    errorMessage: String? = null,
    onFormChanged: () -> Unit = {},
    embedded: Boolean = false,
) {
    val typography = AppTypography()
    var form by remember { mutableStateOf(initialData?.toForm() ?: FormaPagamentoCadastroForm()) }
    var errors by remember { mutableStateOf(emptyMap<String, String>()) }

    fun updateForm(updated: FormaPagamentoCadastroForm) {
        form = updated
        onFormChanged()
        if (errors.isNotEmpty()) errors = validarFormaPagamentoCadastro(updated)
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
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
                    text = "Formas de pagamento",
                    color = TextPrimary,
                    style = typography.titleMedium,
                )
                IconButton(
                    onClick = onBack,
                    enabled = !isSaving,
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
                text =
                    if (recordId == null) {
                        "Cadastrar Forma de Pagamento"
                    } else {
                        "Editar Forma de Pagamento #$recordId"
                    },
                color = TextPrimary,
                style = typography.headlineLarge,
            )

            Spacer(Modifier.height(24.dp))

            errorMessage?.let {
                Text(text = it, color = RedPrimary, style = typography.bodyMedium)
                Spacer(Modifier.height(16.dp))
            }

            FormaPagamentoTextField(
                label = "Nome",
                value = form.nome,
                onValueChange = { updateForm(form.copy(nome = it)) },
                placeholder = "Digite o nome da forma de pagamento",
                error = errors["nome"],
                required = true,
                enabled = !isSaving,
            )

            Spacer(Modifier.height(16.dp))

            FormaPagamentoTextField(
                label = "Descrição",
                value = form.descricao,
                onValueChange = { updateForm(form.copy(descricao = it)) },
                placeholder = "Digite uma descrição (opcional)",
                error = errors["descricao"],
                minLines = 3,
                enabled = !isSaving,
            )

            Spacer(Modifier.height(16.dp))

            FormaPagamentoTextField(
                label = "Limite de parcelas",
                value = form.limiteParcelas,
                onValueChange = {
                    updateForm(form.copy(limiteParcelas = formatarNumeroFormaPagamento(it)))
                },
                placeholder = "Informe o limite de parcelas",
                error = errors["limiteParcelas"],
                keyboardType = KeyboardType.Number,
                required = true,
                enabled = !isSaving,
            )

            Spacer(Modifier.height(16.dp))

            FormaPagamentoTextField(
                label = "Prazo de estorno em dias",
                value = form.prazoEstornoDias,
                onValueChange = {
                    updateForm(form.copy(prazoEstornoDias = formatarNumeroFormaPagamento(it)))
                },
                placeholder = "Informe o prazo (opcional)",
                error = errors["prazoEstornoDias"],
                keyboardType = KeyboardType.Number,
                enabled = !isSaving,
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = form.ativo,
                    onCheckedChange = { updateForm(form.copy(ativo = it)) },
                    enabled = !isSaving,
                    colors =
                        CheckboxDefaults.colors(
                            checkedColor = GreenPrimary,
                            uncheckedColor = GreenPrimary,
                        ),
                )
                Text(
                    text = "Deixar a forma de pagamento ativa?",
                    color = TextPrimary,
                    style = typography.bodyMedium,
                )
            }

            Spacer(Modifier.height(24.dp))

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                if (maxWidth < 320.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        FormaPagamentoSaveButton(
                            modifier = Modifier.fillMaxWidth(),
                            typography = typography,
                            enabled = !isSaving,
                            text = if (recordId == null) "Gravar" else "Salvar alterações",
                            onClick = {
                                val validationErrors = validarFormaPagamentoCadastro(form)
                                errors = validationErrors
                                if (validationErrors.isEmpty()) {
                                    form.toFormaPagamentoCadastroDataOrNull()?.let {
                                        onSave(it)
                                    }
                                }
                            },
                        )
                        FormaPagamentoClearButton(
                            modifier = Modifier.fillMaxWidth(),
                            typography = typography,
                            enabled = !isSaving,
                            text = if (recordId == null) "Limpar campos" else "Cancelar",
                            onClick = {
                                if (recordId != null) {
                                    onBack()
                                } else {
                                    form = FormaPagamentoCadastroForm()
                                    errors = emptyMap()
                                    onFormChanged()
                                }
                            },
                        )
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        FormaPagamentoSaveButton(
                            modifier = Modifier.width(160.dp),
                            typography = typography,
                            enabled = !isSaving,
                            text = if (recordId == null) "Gravar" else "Salvar alterações",
                            onClick = {
                                val validationErrors = validarFormaPagamentoCadastro(form)
                                errors = validationErrors
                                if (validationErrors.isEmpty()) {
                                    form.toFormaPagamentoCadastroDataOrNull()?.let {
                                        onSave(it)
                                    }
                                }
                            },
                        )
                        FormaPagamentoClearButton(
                            modifier = Modifier.width(160.dp),
                            typography = typography,
                            enabled = !isSaving,
                            text = if (recordId == null) "Limpar campos" else "Cancelar",
                            onClick = {
                                if (recordId != null) {
                                    onBack()
                                } else {
                                    form = FormaPagamentoCadastroForm()
                                    errors = emptyMap()
                                    onFormChanged()
                                }
                            },
                        )
                    }
                }
            }

        }
    }
}

@Composable
private fun FormaPagamentoTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    error: String?,
    keyboardType: KeyboardType = KeyboardType.Text,
    required: Boolean = false,
    minLines: Int = 1,
    enabled: Boolean = true,
) {
    val typography = AppTypography()
    Column {
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
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(text = placeholder, color = Gray, style = typography.bodyMedium) },
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
        if (error != null) {
            Text(
                text = error,
                color = RedPrimary,
                style = typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun FormaPagamentoSaveButton(
    modifier: Modifier,
    typography: androidx.compose.material3.Typography,
    enabled: Boolean,
    text: String,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary, contentColor = White),
    ) {
        Text(text, style = typography.bodyMedium)
    }
}

@Composable
private fun FormaPagamentoClearButton(
    modifier: Modifier,
    typography: androidx.compose.material3.Typography,
    enabled: Boolean,
    text: String,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, GreenPrimary),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
    ) {
        Text(text, style = typography.bodyMedium)
    }
}

private fun FormaPagamentoCadastroData.toForm() =
    FormaPagamentoCadastroForm(
        nome = nome,
        ativo = ativo,
        descricao = descricao.orEmpty(),
        limiteParcelas = limiteParcelas.toString(),
        prazoEstornoDias = prazoEstornoDias?.toString().orEmpty(),
    )
