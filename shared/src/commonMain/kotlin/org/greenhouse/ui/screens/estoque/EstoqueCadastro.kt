@file:Suppress("ktlint:standard:function-naming")

package org.greenhouse.ui.screens.estoque

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
import org.greenhouse.ui.screens.estoque.model.EstoqueCadastroData
import org.greenhouse.ui.screens.estoque.model.EstoqueCadastroForm
import org.greenhouse.ui.screens.estoque.validation.formatarQuantidadeEstoque
import org.greenhouse.ui.screens.estoque.validation.toEstoqueCadastroDataOrNull
import org.greenhouse.ui.screens.estoque.validation.validarEstoqueCadastro
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
fun EstoqueCadastro(
    onBack: () -> Unit,
    onSave: (EstoqueCadastroData) -> Unit,
) {
    val typography = AppTypography()
    var form by remember { mutableStateOf(EstoqueCadastroForm()) }
    var errors by remember { mutableStateOf(emptyMap<String, String>()) }

    fun updateForm(updatedForm: EstoqueCadastroForm) {
        form = updatedForm
        if (errors.isNotEmpty()) {
            errors = validarEstoqueCadastro(updatedForm)
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
                text = "Estoque",
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
                text = "Cadastrar Estoque",
                color = TextPrimary,
                style = typography.headlineLarge,
            )

            Spacer(Modifier.height(24.dp))

            EstoqueTextField(
                label = "ID do produto",
                required = true,
                value = form.produtoId,
                onValueChange = { value ->
                    updateForm(form.copy(produtoId = formatarQuantidadeEstoque(value)))
                },
                placeholder = "Informe o ID do produto",
                error = errors["produtoId"],
                keyboardType = KeyboardType.Number,
            )

            Spacer(Modifier.height(16.dp))

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                if (maxWidth < 600.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        EstoqueTextField(
                            label = "Quantidade física",
                            value = form.quantidadeFisica,
                            onValueChange = { value ->
                                updateForm(form.copy(quantidadeFisica = formatarQuantidadeEstoque(value)))
                            },
                            placeholder = "Ex.: 20",
                            error = errors["quantidadeFisica"],
                            keyboardType = KeyboardType.Number,
                        )
                        EstoqueTextField(
                            label = "Quantidade mínima",
                            value = form.quantidadeMinima,
                            onValueChange = { value ->
                                updateForm(form.copy(quantidadeMinima = formatarQuantidadeEstoque(value)))
                            },
                            placeholder = "Ex.: 5",
                            error = errors["quantidadeMinima"],
                            keyboardType = KeyboardType.Number,
                        )
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        EstoqueTextField(
                            label = "Quantidade física",
                            value = form.quantidadeFisica,
                            onValueChange = { value ->
                                updateForm(form.copy(quantidadeFisica = formatarQuantidadeEstoque(value)))
                            },
                            placeholder = "Ex.: 20",
                            error = errors["quantidadeFisica"],
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(1f),
                        )
                        EstoqueTextField(
                            label = "Quantidade mínima",
                            value = form.quantidadeMinima,
                            onValueChange = { value ->
                                updateForm(form.copy(quantidadeMinima = formatarQuantidadeEstoque(value)))
                            },
                            placeholder = "Ex.: 5",
                            error = errors["quantidadeMinima"],
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            EstoqueTextField(
                label = "Localização",
                value = form.localizacao,
                onValueChange = { updateForm(form.copy(localizacao = it)) },
                placeholder = "Ex.: Estante A-01",
                error = errors["localizacao"],
                keyboardType = KeyboardType.Text,
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = form.bloqueado,
                    onCheckedChange = { updateForm(form.copy(bloqueado = it)) },
                    colors = CheckboxDefaults.colors(checkedColor = GreenPrimary, uncheckedColor = GreenPrimary),
                )
                Text("Bloquear este estoque? Um estoque bloqueado não permite reserva de itens.", color = TextPrimary, style = typography.bodyMedium)
            }

            Spacer(Modifier.height(24.dp))

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                if (maxWidth < 340.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        EstoqueSaveButton(
                            modifier = Modifier.fillMaxWidth(),
                            typography = typography,
                            onClick = {
                                val validationErrors = validarEstoqueCadastro(form)
                                errors = validationErrors
                                if (validationErrors.isEmpty()) {
                                    form.toEstoqueCadastroDataOrNull()?.let(onSave)
                                }
                            },
                        )
                        EstoqueClearButton(
                            modifier = Modifier.fillMaxWidth(),
                            typography = typography,
                            onClick = {
                                form = EstoqueCadastroForm()
                                errors = emptyMap()
                            },
                        )
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        EstoqueSaveButton(
                            modifier = Modifier.width(160.dp),
                            typography = typography,
                            onClick = {
                                val validationErrors = validarEstoqueCadastro(form)
                                errors = validationErrors
                                if (validationErrors.isEmpty()) {
                                    form.toEstoqueCadastroDataOrNull()?.let(onSave)
                                }
                            },
                        )
                        EstoqueClearButton(
                            modifier = Modifier.width(160.dp),
                            typography = typography,
                            onClick = {
                                form = EstoqueCadastroForm()
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
private fun EstoqueTextField(
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
private fun EstoqueSaveButton(
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
private fun EstoqueClearButton(
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
