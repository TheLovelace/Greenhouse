@file:Suppress("ktlint:standard:function-naming")

package org.greenhouse.ui.screens.compraFornecedor

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
import org.greenhouse.ui.screens.compraFornecedor.model.CompraFornecedorCadastroData
import org.greenhouse.ui.screens.compraFornecedor.model.CompraFornecedorCadastroForm
import org.greenhouse.ui.screens.compraFornecedor.model.CompraItemForm
import org.greenhouse.ui.screens.compraFornecedor.model.StatusCompraCadastro
import org.greenhouse.ui.screens.compraFornecedor.validation.calcularValorTotalCompra
import org.greenhouse.ui.screens.compraFornecedor.validation.formatarDataCompra
import org.greenhouse.ui.screens.compraFornecedor.validation.formatarDigitosCompra
import org.greenhouse.ui.screens.compraFornecedor.validation.formatarValorCompra
import org.greenhouse.ui.screens.compraFornecedor.validation.toCompraFornecedorCadastroDataOrNull
import org.greenhouse.ui.screens.compraFornecedor.validation.validarCompraFornecedorCadastro
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
fun CompraFornecedorCadastro(
    onBack: () -> Unit,
    onSave: (CompraFornecedorCadastroData) -> Unit,
) {
    val typography = AppTypography()
    var form by remember { mutableStateOf(CompraFornecedorCadastroForm()) }
    var errors by remember { mutableStateOf(emptyMap<String, String>()) }
    var showSuccessMessage by remember { mutableStateOf(false) }

    fun updateForm(updatedForm: CompraFornecedorCadastroForm) {
        form = updatedForm
        showSuccessMessage = false
        if (errors.isNotEmpty()) {
            errors = validarCompraFornecedorCadastro(updatedForm)
        }
    }

    fun updateItem(
        index: Int,
        itemAtualizado: CompraItemForm,
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
                text = "Compras",
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
                text = "Cadastrar Compra de Fornecedor",
                color = TextPrimary,
                style = typography.headlineLarge,
            )

            Spacer(Modifier.height(24.dp))

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                if (maxWidth < 600.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        CompraTextField(
                            label = "ID do funcionário",
                            required = true,
                            value = form.funcionarioId,
                            onValueChange = { value ->
                                updateForm(form.copy(funcionarioId = formatarDigitosCompra(value)))
                            },
                            placeholder = "Informe o ID do funcionário",
                            error = errors["funcionarioId"],
                            keyboardType = KeyboardType.Number,
                        )
                        CompraTextField(
                            label = "ID do fornecedor",
                            required = true,
                            value = form.fornecedorId,
                            onValueChange = { value ->
                                updateForm(form.copy(fornecedorId = formatarDigitosCompra(value)))
                            },
                            placeholder = "Informe o ID do fornecedor",
                            error = errors["fornecedorId"],
                            keyboardType = KeyboardType.Number,
                        )
                        CompraTextField(
                            label = "ID da forma de pagamento",
                            required = true,
                            value = form.formaPagamentoId,
                            onValueChange = { value ->
                                updateForm(form.copy(formaPagamentoId = formatarDigitosCompra(value)))
                            },
                            placeholder = "Informe o ID da forma de pagamento",
                            error = errors["formaPagamentoId"],
                            keyboardType = KeyboardType.Number,
                        )
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        CompraTextField(
                            label = "ID do funcionário",
                            required = true,
                            value = form.funcionarioId,
                            onValueChange = { value ->
                                updateForm(form.copy(funcionarioId = formatarDigitosCompra(value)))
                            },
                            placeholder = "Informe o ID do funcionário",
                            error = errors["funcionarioId"],
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(1f),
                        )
                        CompraTextField(
                            label = "ID do fornecedor",
                            required = true,
                            value = form.fornecedorId,
                            onValueChange = { value ->
                                updateForm(form.copy(fornecedorId = formatarDigitosCompra(value)))
                            },
                            placeholder = "Informe o ID do fornecedor",
                            error = errors["fornecedorId"],
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(1f),
                        )
                        CompraTextField(
                            label = "ID da forma de pagamento",
                            required = true,
                            value = form.formaPagamentoId,
                            onValueChange = { value ->
                                updateForm(form.copy(formaPagamentoId = formatarDigitosCompra(value)))
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
                        CompraTextField(
                            label = "Data da compra",
                            value = form.dataCompra,
                            onValueChange = { value ->
                                updateForm(form.copy(dataCompra = formatarDataCompra(value)))
                            },
                            placeholder = "DD/MM/AAAA",
                            error = errors["dataCompra"],
                            keyboardType = KeyboardType.Number,
                        )
                        CompraTextField(
                            label = "Data prevista de entrega",
                            value = form.dataEntregaPrevista,
                            onValueChange = { value ->
                                updateForm(form.copy(dataEntregaPrevista = formatarDataCompra(value)))
                            },
                            placeholder = "DD/MM/AAAA",
                            error = errors["dataEntregaPrevista"],
                            keyboardType = KeyboardType.Number,
                        )
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        CompraTextField(
                            label = "Data da compra",
                            value = form.dataCompra,
                            onValueChange = { value ->
                                updateForm(form.copy(dataCompra = formatarDataCompra(value)))
                            },
                            placeholder = "DD/MM/AAAA",
                            error = errors["dataCompra"],
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(1f),
                        )
                        CompraTextField(
                            label = "Data prevista de entrega",
                            value = form.dataEntregaPrevista,
                            onValueChange = { value ->
                                updateForm(form.copy(dataEntregaPrevista = formatarDataCompra(value)))
                            },
                            placeholder = "DD/MM/AAAA",
                            error = errors["dataEntregaPrevista"],
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            StatusCompraField(
                status = form.status,
                onStatusChange = { updateForm(form.copy(status = it)) },
                typography = typography,
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
                    CompraTextField(
                        label = "ID do produto",
                        value = item.produtoId,
                        onValueChange = { value ->
                            updateItem(index, item.copy(produtoId = formatarDigitosCompra(value)))
                        },
                        placeholder = "Informe o ID do produto",
                        error = errors["itens[$index].produtoId"],
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f),
                    )
                    CompraTextField(
                        label = "Quantidade",
                        value = item.quantidade,
                        onValueChange = { value ->
                            updateItem(index, item.copy(quantidade = formatarDigitosCompra(value)))
                        },
                        placeholder = "Ex.: 1",
                        error = errors["itens[$index].quantidade"],
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f),
                    )
                    CompraTextField(
                        label = "Valor unitário",
                        value = item.valorUnitario,
                        onValueChange = { value ->
                            updateItem(index, item.copy(valorUnitario = formatarValorCompra(value)))
                        },
                        placeholder = "Ex.: 10,00",
                        error = errors["itens[$index].valorUnitario"],
                        keyboardType = KeyboardType.Decimal,
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
                    updateForm(form.copy(itens = form.itens + CompraItemForm()))
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

            CompraTextField(
                label = "Valor do frete",
                value = form.valorFrete,
                onValueChange = { value ->
                    updateForm(form.copy(valorFrete = formatarValorCompra(value)))
                },
                placeholder = "Ex.: 15,00",
                error = errors["valorFrete"],
                keyboardType = KeyboardType.Decimal,
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = form.freteIncluso,
                    onCheckedChange = { updateForm(form.copy(freteIncluso = it)) },
                    colors = CheckboxDefaults.colors(checkedColor = GreenPrimary, uncheckedColor = GreenPrimary),
                )
                Text(
                    "Frete incluso no valor dos itens",
                    color = TextPrimary,
                    style = typography.bodyMedium,
                )
            }

            val valorTotal = calcularValorTotalCompra(form)
            if (valorTotal != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Valor total calculado: R$ ${valorTotal.replace('.', ',')}",
                    color = TextPrimary,
                    style = typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                )
            }

            Spacer(Modifier.height(16.dp))

            CompraTextField(
                label = "Observação",
                value = form.observacao,
                onValueChange = { updateForm(form.copy(observacao = it)) },
                placeholder = "Digite uma observação (opcional)",
                error = errors["observacao"],
                keyboardType = KeyboardType.Text,
                minLines = 3,
            )

            Spacer(Modifier.height(24.dp))

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                if (maxWidth < 340.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        CompraSaveButton(
                            modifier = Modifier.fillMaxWidth(),
                            typography = typography,
                            onClick = {
                                saveCompraForm(
                                    form = form,
                                    onSave = onSave,
                                    onValidationErrors = { errors = it },
                                    onSaved = { showSuccessMessage = true },
                                )
                            },
                        )
                        CompraClearButton(
                            modifier = Modifier.fillMaxWidth(),
                            typography = typography,
                            onClick = {
                                form = CompraFornecedorCadastroForm()
                                errors = emptyMap()
                                showSuccessMessage = false
                            },
                        )
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        CompraSaveButton(
                            modifier = Modifier.width(160.dp),
                            typography = typography,
                            onClick = {
                                saveCompraForm(
                                    form = form,
                                    onSave = onSave,
                                    onValidationErrors = { errors = it },
                                    onSaved = { showSuccessMessage = true },
                                )
                            },
                        )
                        CompraClearButton(
                            modifier = Modifier.width(160.dp),
                            typography = typography,
                            onClick = {
                                form = CompraFornecedorCadastroForm()
                                errors = emptyMap()
                                showSuccessMessage = false
                            },
                        )
                    }
                }
            }

            if (showSuccessMessage) {
                Spacer(Modifier.height(16.dp))
                CompraSuccessMessage("Formulário validado com sucesso!")
            }
        }
    }
}

@Composable
private fun CompraSuccessMessage(message: String) {
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
private fun CompraTextField(
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
private fun StatusCompraField(
    status: StatusCompraCadastro,
    onStatusChange: (StatusCompraCadastro) -> Unit,
    typography: androidx.compose.material3.Typography,
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        RequiredLabel("Status", required = true, typography = typography)
        Spacer(Modifier.height(6.dp))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
        ) {
            OutlinedTextField(
                value = status.label(),
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
                StatusCompraCadastro.entries.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.label(), color = TextPrimary, style = typography.bodyMedium) },
                        onClick = {
                            onStatusChange(option)
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
private fun CompraSaveButton(
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
private fun CompraClearButton(
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

private fun saveCompraForm(
    form: CompraFornecedorCadastroForm,
    onSave: (CompraFornecedorCadastroData) -> Unit,
    onValidationErrors: (Map<String, String>) -> Unit,
    onSaved: () -> Unit,
) {
    val validationErrors = validarCompraFornecedorCadastro(form)
    onValidationErrors(validationErrors)
    if (validationErrors.isEmpty()) {
        form.toCompraFornecedorCadastroDataOrNull()?.let { data ->
            onSave(data)
            onSaved()
        }
    }
}

private fun StatusCompraCadastro.label(): String =
    when (this) {
        StatusCompraCadastro.PEDIDA -> "Pedida"
        StatusCompraCadastro.RECEBIDA -> "Recebida"
        StatusCompraCadastro.CANCELADA -> "Cancelada"
    }
