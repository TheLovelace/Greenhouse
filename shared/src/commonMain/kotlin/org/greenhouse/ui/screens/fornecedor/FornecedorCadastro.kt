@file:Suppress("ktlint:standard:function-naming")

package org.greenhouse.ui.screens.fornecedor

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
import org.greenhouse.ui.screens.fornecedor.model.FornecedorCadastroData
import org.greenhouse.ui.screens.fornecedor.model.FornecedorCadastroForm
import org.greenhouse.ui.screens.fornecedor.validation.formatarCnpj
import org.greenhouse.ui.screens.fornecedor.validation.formatarDigitosFornecedor
import org.greenhouse.ui.screens.fornecedor.validation.toFornecedorCadastroDataOrNull
import org.greenhouse.ui.screens.fornecedor.validation.validarFornecedorCadastro
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
fun FornecedorCadastro(
    onBack: () -> Unit,
    onSave: (FornecedorCadastroData) -> Unit,
) {
    val typography = AppTypography()
    var form by remember { mutableStateOf(FornecedorCadastroForm()) }
    var errors by remember { mutableStateOf(emptyMap<String, String>()) }
    var showSuccessMessage by remember { mutableStateOf(false) }

    fun updateForm(updatedForm: FornecedorCadastroForm) {
        form = updatedForm
        showSuccessMessage = false
        if (errors.isNotEmpty()) {
            errors = validarFornecedorCadastro(updatedForm)
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
                text = "Fornecedores",
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
                text = "Cadastrar Fornecedor",
                color = TextPrimary,
                style = typography.headlineLarge,
            )

            Spacer(Modifier.height(24.dp))

            FornecedorTextField(
                label = "Nome",
                required = true,
                value = form.nome,
                onValueChange = { updateForm(form.copy(nome = it)) },
                placeholder = "Informe o nome do fornecedor",
                error = errors["nome"],
                keyboardType = KeyboardType.Text,
            )

            Spacer(Modifier.height(16.dp))

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                if (maxWidth < 600.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        FornecedorTextField(
                            label = "CNPJ",
                            value = form.cnpj,
                            onValueChange = { value ->
                                updateForm(form.copy(cnpj = formatarCnpj(value)))
                            },
                            placeholder = "Somente números (14 dígitos)",
                            error = errors["cnpj"],
                            keyboardType = KeyboardType.Number,
                        )
                        FornecedorTextField(
                            label = "Telefone",
                            value = form.telefone,
                            onValueChange = { updateForm(form.copy(telefone = it)) },
                            placeholder = "Ex.: (11) 99999-9999",
                            error = errors["telefone"],
                            keyboardType = KeyboardType.Phone,
                        )
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        FornecedorTextField(
                            label = "CNPJ",
                            value = form.cnpj,
                            onValueChange = { value ->
                                updateForm(form.copy(cnpj = formatarCnpj(value)))
                            },
                            placeholder = "Somente números (14 dígitos)",
                            error = errors["cnpj"],
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(1f),
                        )
                        FornecedorTextField(
                            label = "Telefone",
                            value = form.telefone,
                            onValueChange = { updateForm(form.copy(telefone = it)) },
                            placeholder = "Ex.: (11) 99999-9999",
                            error = errors["telefone"],
                            keyboardType = KeyboardType.Phone,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                if (maxWidth < 600.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        FornecedorTextField(
                            label = "E-mail",
                            value = form.email,
                            onValueChange = { updateForm(form.copy(email = it)) },
                            placeholder = "Ex.: contato@fornecedor.com",
                            error = errors["email"],
                            keyboardType = KeyboardType.Email,
                        )
                        FornecedorTextField(
                            label = "Contato",
                            value = form.contato,
                            onValueChange = { updateForm(form.copy(contato = it)) },
                            placeholder = "Nome do contato no fornecedor",
                            error = errors["contato"],
                            keyboardType = KeyboardType.Text,
                        )
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        FornecedorTextField(
                            label = "E-mail",
                            value = form.email,
                            onValueChange = { updateForm(form.copy(email = it)) },
                            placeholder = "Ex.: contato@fornecedor.com",
                            error = errors["email"],
                            keyboardType = KeyboardType.Email,
                            modifier = Modifier.weight(1f),
                        )
                        FornecedorTextField(
                            label = "Contato",
                            value = form.contato,
                            onValueChange = { updateForm(form.copy(contato = it)) },
                            placeholder = "Nome do contato no fornecedor",
                            error = errors["contato"],
                            keyboardType = KeyboardType.Text,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            FornecedorTextField(
                label = "Prazo médio de entrega (dias)",
                value = form.prazoMedioEntregaDias,
                onValueChange = { value ->
                    updateForm(form.copy(prazoMedioEntregaDias = formatarDigitosFornecedor(value)))
                },
                placeholder = "Ex.: 7",
                error = errors["prazoMedioEntregaDias"],
                keyboardType = KeyboardType.Number,
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = form.ativo,
                    onCheckedChange = { updateForm(form.copy(ativo = it)) },
                    colors = CheckboxDefaults.colors(checkedColor = GreenPrimary, uncheckedColor = GreenPrimary),
                )
                Text("Fornecedor ativo", color = TextPrimary, style = typography.bodyMedium)
            }

            Spacer(Modifier.height(24.dp))

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                if (maxWidth < 340.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        FornecedorSaveButton(
                            modifier = Modifier.fillMaxWidth(),
                            typography = typography,
                            onClick = {
                                saveFornecedorForm(
                                    form = form,
                                    onSave = onSave,
                                    onValidationErrors = { errors = it },
                                    onSaved = { showSuccessMessage = true },
                                )
                            },
                        )
                        FornecedorClearButton(
                            modifier = Modifier.fillMaxWidth(),
                            typography = typography,
                            onClick = {
                                form = FornecedorCadastroForm()
                                errors = emptyMap()
                                showSuccessMessage = false
                            },
                        )
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        FornecedorSaveButton(
                            modifier = Modifier.width(160.dp),
                            typography = typography,
                            onClick = {
                                saveFornecedorForm(
                                    form = form,
                                    onSave = onSave,
                                    onValidationErrors = { errors = it },
                                    onSaved = { showSuccessMessage = true },
                                )
                            },
                        )
                        FornecedorClearButton(
                            modifier = Modifier.width(160.dp),
                            typography = typography,
                            onClick = {
                                form = FornecedorCadastroForm()
                                errors = emptyMap()
                                showSuccessMessage = false
                            },
                        )
                    }
                }
            }

            if (showSuccessMessage) {
                Spacer(Modifier.height(16.dp))
                FornecedorSuccessMessage("Formulário validado com sucesso!")
            }
        }
    }
}

@Composable
private fun FornecedorSuccessMessage(message: String) {
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
private fun FornecedorTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    error: String?,
    keyboardType: KeyboardType,
    modifier: Modifier = Modifier,
    required: Boolean = false,
) {
    val typography = AppTypography()
    Column(modifier = modifier) {
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
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(text = placeholder, color = Gray, style = typography.bodyMedium) },
            isError = error != null,
            shape = RoundedCornerShape(12.dp),
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
            singleLine = true,
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
private fun FornecedorSaveButton(
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
private fun FornecedorClearButton(
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

private fun saveFornecedorForm(
    form: FornecedorCadastroForm,
    onSave: (FornecedorCadastroData) -> Unit,
    onValidationErrors: (Map<String, String>) -> Unit,
    onSaved: () -> Unit,
) {
    val validationErrors = validarFornecedorCadastro(form)
    onValidationErrors(validationErrors)
    if (validationErrors.isEmpty()) {
        form.toFornecedorCadastroDataOrNull()?.let { data ->
            onSave(data)
            onSaved()
        }
    }
}
