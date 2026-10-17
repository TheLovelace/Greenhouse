@file:Suppress("ktlint:standard:function-naming")

package org.greenhouse.ui.screens.pedido

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
import greenhouse.shared.generated.resources.icon_adicionar
import greenhouse.shared.generated.resources.icon_confirmar
import greenhouse.shared.generated.resources.icon_excluir
import greenhouse.shared.generated.resources.icon_voltar
import org.greenhouse.ui.screens.pedido.model.ModalidadePedidoCadastro
import org.greenhouse.ui.screens.pedido.model.PedidoCadastroData
import org.greenhouse.ui.screens.pedido.model.PedidoCadastroForm
import org.greenhouse.ui.screens.pedido.model.PedidoItemForm
import org.greenhouse.ui.screens.pedido.validation.formatarDataBrasileira
import org.greenhouse.ui.screens.pedido.validation.formatarDigitos
import org.greenhouse.ui.screens.pedido.validation.toPedidoCadastroDataOrNull
import org.greenhouse.ui.screens.pedido.validation.validarPedidoCadastro
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
fun PedidoCadastro(
    onBack: () -> Unit,
    onSave: (PedidoCadastroData) -> Unit,
) {
    val typography = AppTypography()
    var form by remember { mutableStateOf(PedidoCadastroForm()) }
    var errors by remember { mutableStateOf(emptyMap<String, String>()) }
    var showSuccessMessage by remember { mutableStateOf(false) }

    fun updateForm(updatedForm: PedidoCadastroForm) {
        form = updatedForm
        showSuccessMessage = false
        if (errors.isNotEmpty()) {
            errors = validarPedidoCadastro(updatedForm)
        }
    }

    fun updateItem(
        index: Int,
        itemAtualizado: PedidoItemForm,
    ) {
        val novosItens = form.itens.toMutableList()
        novosItens[index] = itemAtualizado
        updateForm(form.copy(itens = novosItens))
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
                text = "Pedidos",
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
                text = "Cadastrar Pedido",
                color = TextPrimary,
                style = typography.headlineLarge,
            )

            Spacer(Modifier.height(24.dp))

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                if (maxWidth < 600.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        PedidoTextField(
                            label = "ID do funcionário",
                            required = true,
                            value = form.funcionarioId,
                            onValueChange = { value ->
                                updateForm(form.copy(funcionarioId = formatarDigitos(value)))
                            },
                            placeholder = "Informe o ID do funcionário",
                            error = errors["funcionarioId"],
                            keyboardType = KeyboardType.Number,
                        )
                        PedidoTextField(
                            label = "ID da forma de pagamento",
                            required = true,
                            value = form.formaPagamentoId,
                            onValueChange = { value ->
                                updateForm(form.copy(formaPagamentoId = formatarDigitos(value)))
                            },
                            placeholder = "Informe o ID da forma de pagamento",
                            error = errors["formaPagamentoId"],
                            keyboardType = KeyboardType.Number,
                        )
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        PedidoTextField(
                            label = "ID do funcionário",
                            required = true,
                            value = form.funcionarioId,
                            onValueChange = { value ->
                                updateForm(form.copy(funcionarioId = formatarDigitos(value)))
                            },
                            placeholder = "Informe o ID do funcionário",
                            error = errors["funcionarioId"],
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(1f),
                        )
                        PedidoTextField(
                            label = "ID da forma de pagamento",
                            required = true,
                            value = form.formaPagamentoId,
                            onValueChange = { value ->
                                updateForm(form.copy(formaPagamentoId = formatarDigitos(value)))
                            },
                            placeholder = "Informe o ID da forma de pagamento",
                            error = errors["formaPagamentoId"],
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                if (maxWidth < 600.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        PedidoTextField(
                            label = "ID do cliente",
                            value = form.clienteId,
                            onValueChange = { value ->
                                updateForm(form.copy(clienteId = formatarDigitos(value)))
                            },
                            placeholder = "ID do cliente (opcional)",
                            error = errors["clienteId"],
                            keyboardType = KeyboardType.Number,
                        )
                        PedidoTextField(
                            label = "ID do desconto",
                            value = form.descontoId,
                            onValueChange = { value ->
                                updateForm(form.copy(descontoId = formatarDigitos(value)))
                            },
                            placeholder = "ID do desconto (opcional)",
                            error = errors["descontoId"],
                            keyboardType = KeyboardType.Number,
                        )
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        PedidoTextField(
                            label = "ID do cliente",
                            value = form.clienteId,
                            onValueChange = { value ->
                                updateForm(form.copy(clienteId = formatarDigitos(value)))
                            },
                            placeholder = "ID do cliente (opcional)",
                            error = errors["clienteId"],
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(1f),
                        )
                        PedidoTextField(
                            label = "ID do desconto",
                            value = form.descontoId,
                            onValueChange = { value ->
                                updateForm(form.copy(descontoId = formatarDigitos(value)))
                            },
                            placeholder = "ID do desconto (opcional)",
                            error = errors["descontoId"],
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            ModalidadeField(
                modalidade = form.modalidade,
                onModalidadeChange = { updateForm(form.copy(modalidade = it)) },
                typography = typography,
            )

            Spacer(Modifier.height(16.dp))

            DateInputField(
                label = "Data de retirada prevista",
                value = form.dataRetiradaPrevista,
                error = errors["dataRetiradaPrevista"],
                onValueChange = { value ->
                    updateForm(form.copy(dataRetiradaPrevista = formatarDataBrasileira(value)))
                },
            )

            Spacer(Modifier.height(16.dp))

            RequiredLabel("Itens", required = true, typography = typography)
            FieldError(errors["itens"])
            Spacer(Modifier.height(6.dp))

            form.itens.forEachIndexed { index, item ->
                if (index > 0) Spacer(Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    PedidoTextField(
                        label = "ID do produto",
                        value = item.produtoId,
                        onValueChange = { value ->
                            updateItem(index, item.copy(produtoId = formatarDigitos(value)))
                        },
                        placeholder = "Informe o ID do produto",
                        error = errors["itens[$index].produtoId"],
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f),
                    )
                    PedidoTextField(
                        label = "Quantidade",
                        value = item.quantidade,
                        onValueChange = { value ->
                            updateItem(index, item.copy(quantidade = formatarDigitos(value)))
                        },
                        placeholder = "Ex.: 1",
                        error = errors["itens[$index].quantidade"],
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f),
                    )
                    if (form.itens.size > 1) {
                        IconButton(
                            onClick = {
                                updateForm(form.copy(itens = form.itens.filterIndexed { i, _ -> i != index }))
                            },
                            modifier = Modifier.padding(top = 30.dp).size(48.dp),
                        ) {
                            Image(
                                painter = painterResource(Res.drawable.icon_excluir),
                                contentDescription = "Remover item",
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    updateForm(form.copy(itens = form.itens + PedidoItemForm()))
                },
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, GreenPrimary),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
            ) {
                Image(
                    painter = painterResource(Res.drawable.icon_adicionar),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text("Adicionar item", style = typography.bodyMedium)
            }

            Spacer(Modifier.height(16.dp))

            PedidoTextField(
                label = "Número de volumes",
                value = form.numeroVolumes,
                onValueChange = { value ->
                    updateForm(form.copy(numeroVolumes = formatarDigitos(value)))
                },
                placeholder = "Informe o número de volumes",
                error = errors["numeroVolumes"],
                keyboardType = KeyboardType.Number,
            )

            Spacer(Modifier.height(16.dp))

            PedidoTextField(
                label = "Observação",
                value = form.observacao,
                onValueChange = { updateForm(form.copy(observacao = it)) },
                placeholder = "Digite uma observação (opcional)",
                error = errors["observacao"],
                keyboardType = KeyboardType.Text,
                minLines = 3,
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = form.embalagemPresente,
                    onCheckedChange = { updateForm(form.copy(embalagemPresente = it)) },
                    colors = CheckboxDefaults.colors(checkedColor = GreenPrimary, uncheckedColor = GreenPrimary),
                )
                Text("Embalagem de presente", color = TextPrimary, style = typography.bodyMedium)
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
                                form = PedidoCadastroForm()
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
                                form = PedidoCadastroForm()
                                errors = emptyMap()
                                showSuccessMessage = false
                            },
                        )
                    }
                }
            }

            if (showSuccessMessage) {
                Spacer(Modifier.height(16.dp))
                CadastroSuccessMessage("Pedido cadastrado com sucesso!")
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
private fun PedidoTextField(
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
    PedidoTextField(
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
private fun ModalidadeField(
    modalidade: ModalidadePedidoCadastro,
    onModalidadeChange: (ModalidadePedidoCadastro) -> Unit,
    typography: androidx.compose.material3.Typography,
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        RequiredLabel("Modalidade", required = true, typography = typography)
        Spacer(Modifier.height(6.dp))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
        ) {
            OutlinedTextField(
                value = modalidade.label(),
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
                ModalidadePedidoCadastro.entries.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.label(), color = TextPrimary, style = typography.bodyMedium) },
                        onClick = {
                            onModalidadeChange(option)
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
    form: PedidoCadastroForm,
    onSave: (PedidoCadastroData) -> Unit,
    onValidationErrors: (Map<String, String>) -> Unit,
    onSaved: () -> Unit,
) {
    val validationErrors = validarPedidoCadastro(form)
    onValidationErrors(validationErrors)
    if (validationErrors.isEmpty()) {
        form.toPedidoCadastroDataOrNull()?.let { data ->
            onSave(data)
            onSaved()
        }
    }
}

private fun ModalidadePedidoCadastro.label(): String =
    when (this) {
        ModalidadePedidoCadastro.IMEDIATA -> "Imediata"
        ModalidadePedidoCadastro.AGENDADA -> "Agendada"
    }
