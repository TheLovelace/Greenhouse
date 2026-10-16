@file:Suppress("ktlint:standard:function-naming")

package org.greenhouse.ui.screens.funcionario

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
import org.greenhouse.ui.screens.funcionario.model.FuncionarioCadastroData
import org.greenhouse.ui.screens.funcionario.model.FuncionarioCadastroForm
import org.greenhouse.ui.screens.funcionario.validation.toFuncionarioCadastroDataOrNull
import org.greenhouse.ui.screens.funcionario.validation.validarFuncionarioCadastro
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
fun FuncionarioCadastro(
    onBack: () -> Unit,
    onSave: (FuncionarioCadastroData) -> Unit,
) {
    val typography = AppTypography()
    var form by remember { mutableStateOf(FuncionarioCadastroForm()) }
    var errors by remember { mutableStateOf(emptyMap<String, String>()) }
    var showSuccessMessage by remember { mutableStateOf(false) }

    fun updateForm(updated: FuncionarioCadastroForm) {
        form = updated
        showSuccessMessage = false
        if (errors.isNotEmpty()) errors = validarFuncionarioCadastro(updated)
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
                text = "Funcionários",
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
                text = "Cadastrar Funcionário",
                color = TextPrimary,
                style = typography.headlineLarge,
            )

            Spacer(Modifier.height(24.dp))

            FuncionarioTextField(
                label = "Nome",
                value = form.nome,
                onValueChange = { updateForm(form.copy(nome = it)) },
                placeholder = "Digite o nome do funcionário",
                error = errors["nome"],
                required = true,
            )

            Spacer(Modifier.height(16.dp))

            FuncionarioTextField(
                label = "CPF",
                value = form.cpf,
                onValueChange = { updateForm(form.copy(cpf = it.filter(Char::isDigit).take(11))) },
                placeholder = "Informe os 11 dígitos do CPF",
                error = errors["cpf"],
                keyboardType = KeyboardType.Number,
                required = true,
            )

            Spacer(Modifier.height(16.dp))

            FuncionarioTextField(
                label = "E-mail",
                value = form.email,
                onValueChange = { updateForm(form.copy(email = it)) },
                placeholder = "Digite o e-mail",
                error = errors["email"],
                keyboardType = KeyboardType.Email,
                required = true,
            )

            Spacer(Modifier.height(16.dp))

            FuncionarioTextField(
                label = "Telefone",
                value = form.telefone,
                onValueChange = { updateForm(form.copy(telefone = it)) },
                placeholder = "Informe o telefone (opcional)",
                error = errors["telefone"],
                keyboardType = KeyboardType.Phone,
            )

            Spacer(Modifier.height(16.dp))

            FuncionarioTextField(
                label = "Cargo",
                value = form.cargo,
                onValueChange = { updateForm(form.copy(cargo = it)) },
                placeholder = "Informe o cargo (opcional)",
                error = errors["cargo"],
            )

            Spacer(Modifier.height(16.dp))

            FuncionarioTextField(
                label = "Data de nascimento (AAAA-MM-DD)",
                value = form.dataDeNascimento,
                onValueChange = { updateForm(form.copy(dataDeNascimento = it)) },
                placeholder = "Ex.: 1990-05-21",
                error = errors["dataDeNascimento"],
                keyboardType = KeyboardType.Number,
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = form.ativo,
                    onCheckedChange = { updateForm(form.copy(ativo = it)) },
                    colors =
                        CheckboxDefaults.colors(
                            checkedColor = GreenPrimary,
                            uncheckedColor = GreenPrimary,
                        ),
                )
                Text(
                    text = "Deixar o funcionário ativo?",
                    color = TextPrimary,
                    style = typography.bodyMedium,
                )
            }

            Spacer(Modifier.height(24.dp))

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                if (maxWidth < 320.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        FuncionarioSaveButton(
                            modifier = Modifier.fillMaxWidth(),
                            typography = typography,
                            onClick = {
                                val validationErrors = validarFuncionarioCadastro(form)
                                errors = validationErrors
                                if (validationErrors.isEmpty()) {
                                    form.toFuncionarioCadastroDataOrNull()?.let {
                                        onSave(it)
                                        showSuccessMessage = true
                                    }
                                }
                            },
                        )
                        FuncionarioClearButton(
                            modifier = Modifier.fillMaxWidth(),
                            typography = typography,
                            onClick = {
                                form = FuncionarioCadastroForm()
                                errors = emptyMap()
                                showSuccessMessage = false
                            },
                        )
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        FuncionarioSaveButton(
                            modifier = Modifier.width(160.dp),
                            typography = typography,
                            onClick = {
                                val validationErrors = validarFuncionarioCadastro(form)
                                errors = validationErrors
                                if (validationErrors.isEmpty()) {
                                    form.toFuncionarioCadastroDataOrNull()?.let {
                                        onSave(it)
                                        showSuccessMessage = true
                                    }
                                }
                            },
                        )
                        FuncionarioClearButton(
                            modifier = Modifier.width(160.dp),
                            typography = typography,
                            onClick = {
                                form = FuncionarioCadastroForm()
                                errors = emptyMap()
                                showSuccessMessage = false
                            },
                        )
                    }
                }
            }

            if (showSuccessMessage) {
                Spacer(Modifier.height(16.dp))
                CadastroSuccessMessage("Funcionário cadastrado com sucesso!")
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
private fun FuncionarioTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    error: String?,
    keyboardType: KeyboardType = KeyboardType.Text,
    required: Boolean = false,
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
private fun FuncionarioSaveButton(
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
private fun FuncionarioClearButton(
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
