@file:Suppress("ktlint:standard:function-naming")

package org.greenhouse.ui.screens.produto

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
import org.greenhouse.ui.screens.produto.model.ProdutoCadastroData
import org.greenhouse.ui.screens.produto.model.ProdutoCadastroForm
import org.greenhouse.ui.screens.produto.validation.formatarPrazoPreparoProduto
import org.greenhouse.ui.screens.produto.validation.formatarPrecoProduto
import org.greenhouse.ui.screens.produto.validation.toProdutoCadastroDataOrNull
import org.greenhouse.ui.screens.produto.validation.validarProdutoCadastro
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
fun ProdutoCadastro(
    onBack: () -> Unit,
    onSave: (ProdutoCadastroData) -> Unit,
) {
    val typography = AppTypography()
    var form by remember { mutableStateOf(ProdutoCadastroForm()) }
    var errors by remember { mutableStateOf(emptyMap<String, String>()) }
    var showSuccessMessage by remember { mutableStateOf(false) }

    fun updateForm(updatedForm: ProdutoCadastroForm) {
        form = updatedForm
        showSuccessMessage = false
        if (errors.isNotEmpty()) errors = validarProdutoCadastro(updatedForm)
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
            Text("Produtos", color = TextPrimary, style = typography.titleMedium)
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
            Text("Cadastrar Produto", color = TextPrimary, style = typography.headlineLarge)
            Spacer(Modifier.height(24.dp))

            ProdutoTextField(
                label = "Nome",
                required = true,
                value = form.nome,
                onValueChange = { updateForm(form.copy(nome = it)) },
                placeholder = "Digite o nome do produto",
                error = errors["nome"],
            )

            Spacer(Modifier.height(16.dp))

            ProdutoTextField(
                label = "Código",
                value = form.codigo,
                onValueChange = { updateForm(form.copy(codigo = it)) },
                placeholder = "Código opcional",
                error = errors["codigo"],
            )

            Spacer(Modifier.height(16.dp))

            ProdutoTextField(
                label = "Descrição",
                value = form.descricao,
                onValueChange = { updateForm(form.copy(descricao = it)) },
                placeholder = "Descrição (opcional)",
                error = errors["descricao"],
                minLines = 3,
            )

            Spacer(Modifier.height(16.dp))

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                if (maxWidth < 600.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        ProdutoTextField(
                            label = "Preço",
                            required = true,
                            value = form.preco,
                            onValueChange = { updateForm(form.copy(preco = formatarPrecoProduto(it))) },
                            placeholder = "Ex.: 25,50",
                            error = errors["preco"],
                            keyboardType = KeyboardType.Decimal,
                        )
                        ProdutoTextField(
                            label = "Prazo de preparo (minutos)",
                            value = form.prazoPreparoMinutos,
                            onValueChange = {
                                updateForm(form.copy(prazoPreparoMinutos = formatarPrazoPreparoProduto(it)))
                            },
                            placeholder = "Opcional",
                            error = errors["prazoPreparoMinutos"],
                            keyboardType = KeyboardType.Number,
                        )
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        ProdutoTextField(
                            label = "Preço",
                            required = true,
                            value = form.preco,
                            onValueChange = { updateForm(form.copy(preco = formatarPrecoProduto(it))) },
                            placeholder = "Ex.: 25,50",
                            error = errors["preco"],
                            keyboardType = KeyboardType.Decimal,
                            modifier = Modifier.weight(1f),
                        )
                        ProdutoTextField(
                            label = "Prazo de preparo (minutos)",
                            value = form.prazoPreparoMinutos,
                            onValueChange = {
                                updateForm(form.copy(prazoPreparoMinutos = formatarPrazoPreparoProduto(it)))
                            },
                            placeholder = "Opcional",
                            error = errors["prazoPreparoMinutos"],
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = form.ativo,
                    onCheckedChange = { updateForm(form.copy(ativo = it)) },
                    colors = CheckboxDefaults.colors(checkedColor = GreenPrimary, uncheckedColor = GreenPrimary),
                )
                Text("Produto ativo", color = TextPrimary, style = typography.bodyMedium)
            }

            Spacer(Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = {
                        val validationErrors = validarProdutoCadastro(form)
                        errors = validationErrors
                        if (validationErrors.isEmpty()) {
                            form.toProdutoCadastroDataOrNull()?.let { data ->
                                onSave(data)
                                showSuccessMessage = true
                            }
                        }
                    },
                    modifier = Modifier.width(160.dp).height(52.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary, contentColor = White),
                ) {
                    Text("Gravar", style = typography.bodyMedium)
                }
                OutlinedButton(
                    onClick = {
                        form = ProdutoCadastroForm()
                        errors = emptyMap()
                        showSuccessMessage = false
                    },
                    modifier = Modifier.width(160.dp).height(52.dp),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, GreenPrimary),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                ) {
                    Text("Limpar campos", style = typography.bodyMedium)
                }
            }

            if (showSuccessMessage) {
                Spacer(Modifier.height(16.dp))
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
                    Text("Produto cadastrado com sucesso!", color = GreenPrimary, style = typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun ProdutoTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    error: String?,
    modifier: Modifier = Modifier,
    required: Boolean = false,
    minLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    val typography = AppTypography()
    Column(modifier = modifier) {
        Row {
            Text(label, color = TextPrimary, style = typography.bodyMedium)
            if (required) {
                Text(
                    " *",
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
            placeholder = { Text(placeholder, color = Gray, style = typography.bodyMedium) },
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
            Text(error, color = RedPrimary, style = typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
        }
    }
}
