@file:Suppress("ktlint:standard:function-naming")

package org.greenhouse.ui.screens.cliente

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
import greenhouse.shared.generated.resources.icon_confirmar
import greenhouse.shared.generated.resources.icon_voltar
import org.greenhouse.ui.screens.cliente.model.ClienteCadastroData
import org.greenhouse.ui.screens.cliente.model.ClienteCadastroForm
import org.greenhouse.ui.screens.cliente.validation.formatarCpf
import org.greenhouse.ui.screens.cliente.validation.toClienteCadastroDataOrNull
import org.greenhouse.ui.screens.cliente.validation.validarClienteCadastro
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
fun ClienteCadastro(
    onBack: () -> Unit,
    onSave: (ClienteCadastroData) -> Unit,
) {
    val typography = AppTypography()
    var form by remember { mutableStateOf(ClienteCadastroForm()) }
    var errors by remember { mutableStateOf(emptyMap<String, String>()) }
    var showSuccessMessage by remember { mutableStateOf(false) }

    fun updateForm(updatedForm: ClienteCadastroForm) {
        form = updatedForm
        showSuccessMessage = false
        if (errors.isNotEmpty()) {
            errors = validarClienteCadastro(updatedForm)
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
                text = "Clientes",
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
                text = "Cadastrar Cliente",
                color = TextPrimary,
                style = typography.headlineLarge,
            )

            Spacer(Modifier.height(24.dp))

            ClienteTextField(
                label = "Nome",
                required = true,
                value = form.nome,
                onValueChange = { updateForm(form.copy(nome = it)) },
                placeholder = "Digite o nome do cliente",
                error = errors["nome"],
                keyboardType = KeyboardType.Text,
            )

            Spacer(Modifier.height(16.dp))

            ClienteTextField(
                label = "Telefone",
                required = true,
                value = form.telefone,
                onValueChange = { updateForm(form.copy(telefone = it)) },
                placeholder = "Ex.: (11) 98765-4321",
                error = errors["telefone"],
                keyboardType = KeyboardType.Phone,
            )

            Spacer(Modifier.height(16.dp))

            ClienteTextField(
                label = "CPF",
                value = form.cpf,
                onValueChange = { value ->
                    updateForm(form.copy(cpf = formatarCpf(value)))
                },
                placeholder = "CPF (opcional)",
                error = errors["cpf"],
                keyboardType = KeyboardType.Number,
            )

            Spacer(Modifier.height(16.dp))

            ClienteTextField(
                label = "E-mail",
                value = form.email,
                onValueChange = { updateForm(form.copy(email = it)) },
                placeholder = "E-mail (opcional)",
                error = errors["email"],
                keyboardType = KeyboardType.Email,
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = form.whatsapp,
                    onCheckedChange = { updateForm(form.copy(whatsapp = it)) },
                    colors = CheckboxDefaults.colors(checkedColor = GreenPrimary, uncheckedColor = GreenPrimary),
                )
                Text("Contato possui WhatsApp", color = TextPrimary, style = typography.bodyMedium)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = form.ativo,
                    onCheckedChange = { updateForm(form.copy(ativo = it)) },
                    colors = CheckboxDefaults.colors(checkedColor = GreenPrimary, uncheckedColor = GreenPrimary),
                )
                Text("Cliente ativo", color = TextPrimary, style = typography.bodyMedium)
            }

            Spacer(Modifier.height(24.dp))

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                if (maxWidth < 320.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SaveButton(
                            modifier = Modifier.width(160.dp),
                            typography = typography,
                            onClick = {
                                saveForm(
                                    form = form,
                                    onSave = onSave,
                                    onValidationErrors = { errors = it },
                                    onSaved = { showSuccessMessage = true },
                                )
                            },
                        )
                        ClearButton(
                            modifier = Modifier.width(160.dp),
                            typography = typography,
                            onClick = {
                                form = ClienteCadastroForm()
                                errors = emptyMap()
                                showSuccessMessage = false
                            },
                        )
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SaveButton(
                            modifier = Modifier.width(160.dp),
                            typography = typography,
                            onClick = {
                                saveForm(
                                    form = form,
                                    onSave = onSave,
                                    onValidationErrors = { errors = it },
                                    onSaved = { showSuccessMessage = true },
                                )
                            },
                        )
                        ClearButton(
                            modifier = Modifier.width(160.dp),
                            typography = typography,
                            onClick = {
                                form = ClienteCadastroForm()
                                errors = emptyMap()
                                showSuccessMessage = false
                            },
                        )
                    }
                }
            }

            if (showSuccessMessage) {
                Spacer(Modifier.height(16.dp))
                CadastroSuccessMessage("Cliente cadastrado com sucesso!")
            }
        }
    }
}

@Composable
private fun CadastroSuccessMessage(message: String) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(GreenSecondary.copy(alpha = 0.16f), RoundedCornerShape(12.dp))
                .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Image(
            painter = painterResource(Res.drawable.icon_confirmar),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
        )
        Text(text = message, color = GreenPrimary, style = AppTypography().bodyMedium)
    }
}

@Composable
private fun ClienteTextField(
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

private fun saveForm(
    form: ClienteCadastroForm,
    onSave: (ClienteCadastroData) -> Unit,
    onValidationErrors: (Map<String, String>) -> Unit,
    onSaved: () -> Unit,
) {
    val validationErrors = validarClienteCadastro(form)
    onValidationErrors(validationErrors)
    if (validationErrors.isEmpty()) {
        form.toClienteCadastroDataOrNull()?.let { data ->
            onSave(data)
            onSaved()
        }
    }
}
