@file:Suppress("ktlint:standard:function-naming")

package org.greenhouse.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import greenhouse.shared.generated.resources.Res
import greenhouse.shared.generated.resources.icon_adicionar_light
import greenhouse.shared.generated.resources.icon_editar
import greenhouse.shared.generated.resources.icon_excluir_light
import greenhouse.shared.generated.resources.icon_filtro
import greenhouse.shared.generated.resources.icon_pesquisa
import greenhouse.shared.generated.resources.icon_voltar
import org.greenhouse.ui.theme.AppTypography
import org.greenhouse.ui.theme.Background
import org.greenhouse.ui.theme.GreenPrimary
import org.greenhouse.ui.theme.GreenSecondary
import org.greenhouse.ui.theme.PinkSecondary
import org.greenhouse.ui.theme.RedPrimary
import org.greenhouse.ui.theme.TextPrimary
import org.greenhouse.ui.theme.TextSecondary
import org.greenhouse.ui.theme.White
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

data class CadastroListagemItem(
    val id: Long,
    val titulo: String,
    val detalhes: List<String>,
    val ativo: Boolean,
    val status: String = if (ativo) "Ativo" else "Inativo",
    val corStatus: Color = if (ativo) GreenSecondary else RedPrimary,
)

@Composable
fun CadastroListagem(
    titulo: String,
    registros: List<CadastroListagemItem>,
    placeholderBusca: String,
    mensagemListaVazia: String,
    mensagemInativacao: (CadastroListagemItem) -> String,
    isCarregando: Boolean,
    isProcessando: Boolean,
    mensagemErro: String?,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    onCreate: () -> Unit,
    onEdit: (Long) -> Unit,
    onDeactivate: (Long) -> Unit,
    buscaRemota: String? = null,
    onBuscaRemota: (String) -> Unit = {},
    filtrosStatus: Map<String, String> = mapOf("Ativos" to "Ativo", "Inativos" to "Inativo"),
    rotuloAcaoPrincipal: String = "Editar",
    iconeAcaoPrincipal: DrawableResource = Res.drawable.icon_editar,
    rotuloAcaoDestrutiva: String = "Inativar",
    mostrarAcaoDestrutiva: (CadastroListagemItem) -> Boolean = { it.ativo },
) {
    val typography = AppTypography()
    var busca by remember { mutableStateOf("") }
    var filtro by remember { mutableStateOf<String?>(null) }
    var filtroExpandido by remember { mutableStateOf(false) }
    var registroParaConfirmar by remember { mutableStateOf<CadastroListagemItem?>(null) }

    val registrosFiltrados =
        registros.filter { registro ->
            val termo = busca.trim()
            val correspondeBusca =
                buscaRemota != null ||
                    termo.isEmpty() ||
                    registro.id.toString().contains(termo, ignoreCase = true) ||
                    registro.titulo.contains(termo, ignoreCase = true) ||
                    registro.detalhes.any { it.contains(termo, ignoreCase = true) }
            correspondeBusca && (filtro == null || filtro == registro.status)
        }

    BoxWithConstraints(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Background)
                .padding(horizontal = 24.dp, vertical = 18.dp),
    ) {
        val compactLayout = maxWidth < 600.dp
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxWidth().height(52.dp), contentAlignment = Alignment.Center) {
                Text(titulo, color = TextPrimary, style = typography.titleMedium)
                IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart).size(48.dp)) {
                    Image(
                        painter = painterResource(Res.drawable.icon_voltar),
                        contentDescription = "Voltar",
                        modifier = Modifier.size(32.dp),
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = buscaRemota ?: busca,
                    onValueChange = { termo ->
                        if (buscaRemota == null) busca = termo else onBuscaRemota(termo)
                    },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(placeholderBusca, color = TextPrimary.copy(alpha = 0.65f)) },
                    leadingIcon = {
                        Image(
                            painter = painterResource(Res.drawable.icon_pesquisa),
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors =
                        androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedBorderColor = PinkSecondary,
                            unfocusedBorderColor = GreenPrimary,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = GreenPrimary,
                        ),
                )
                Box {
                    IconButton(
                        onClick = { filtroExpandido = true },
                        modifier =
                            Modifier
                                .size(52.dp)
                                .border(BorderStroke(1.dp, GreenPrimary), RoundedCornerShape(14.dp)),
                    ) {
                        Image(
                            painter = painterResource(Res.drawable.icon_filtro),
                            contentDescription = "Filtrar registros",
                            modifier = Modifier.size(26.dp),
                        )
                    }
                    DropdownMenu(expanded = filtroExpandido, onDismissRequest = { filtroExpandido = false }) {
                        (mapOf("Todos" to null) + filtrosStatus).forEach { (rotulo, status) ->
                            DropdownMenuItem(
                                text = { Text(rotulo, color = TextPrimary) },
                                onClick = {
                                    filtro = status
                                    filtroExpandido = false
                                },
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))
            when {
                isCarregando -> {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = GreenPrimary)
                    }
                }
                mensagemErro != null -> {
                    Column(
                        modifier = Modifier.weight(1f).fillMaxWidth().padding(top = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(mensagemErro, color = RedPrimary, style = typography.bodyLarge)
                        OutlinedButton(onClick = onRetry, enabled = !isProcessando) {
                            Text("Tentar novamente", color = GreenPrimary)
                        }
                    }
                }
                registrosFiltrados.isEmpty() -> {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.TopStart) {
                        Text(
                            text = if (registros.isEmpty()) mensagemListaVazia else "Nenhum registro encontrado.",
                            color = TextPrimary,
                            style = typography.bodyLarge,
                            modifier = Modifier.padding(top = 12.dp),
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentPadding = PaddingValues(bottom = 92.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        items(registrosFiltrados, key = { it.id }) { registro ->
                            CadastroRegistroCard(
                                registro = registro,
                                isProcessando = isProcessando,
                                onEdit = { onEdit(registro.id) },
                                onDeactivate = { registroParaConfirmar = registro },
                                rotuloAcaoPrincipal = rotuloAcaoPrincipal,
                                iconeAcaoPrincipal = iconeAcaoPrincipal,
                                rotuloAcaoDestrutiva = rotuloAcaoDestrutiva,
                                mostrarAcaoDestrutiva = mostrarAcaoDestrutiva(registro),
                            )
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { if (!isProcessando) onCreate() },
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 10.dp, bottom = 48.dp),
            shape = RoundedCornerShape(18.dp),
            containerColor = GreenPrimary,
            contentColor = Color.White,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = if (compactLayout) 0.dp else 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(Res.drawable.icon_adicionar_light),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                )
                if (!compactLayout) {
                    Text("Cadastrar", color = TextSecondary, style = typography.labelLarge)
                }
            }
        }
    }

    registroParaConfirmar?.let { registro ->
        CadastroConfirmacao(
            titulo = "$rotuloAcaoDestrutiva registro?",
            mensagem = mensagemInativacao(registro),
            rotuloConfirmar = rotuloAcaoDestrutiva,
            onConfirm = {
                onDeactivate(registro.id)
                registroParaConfirmar = null
            },
            onDismiss = { registroParaConfirmar = null },
            isProcessando = isProcessando,
        )
    }
}

@Composable
internal fun CadastroConfirmacao(
    titulo: String,
    mensagem: String,
    rotuloConfirmar: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isProcessando: Boolean = false,
    destrutiva: Boolean = true,
    rotuloCancelar: String = "Cancelar",
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(titulo) },
        text = { Text(mensagem) },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !isProcessando) {
                Text(rotuloConfirmar, color = if (destrutiva) RedPrimary else GreenPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(rotuloCancelar, color = GreenPrimary)
            }
        },
    )
}

@Composable
private fun CadastroRegistroCard(
    registro: CadastroListagemItem,
    isProcessando: Boolean,
    onEdit: () -> Unit,
    onDeactivate: () -> Unit,
    rotuloAcaoPrincipal: String,
    iconeAcaoPrincipal: DrawableResource,
    rotuloAcaoDestrutiva: String,
    mostrarAcaoDestrutiva: Boolean,
) {
    val typography = AppTypography()
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, GreenPrimary),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(registro.titulo, modifier = Modifier.weight(1f), color = TextPrimary, style = typography.titleMedium)
                Card(
                    shape = RoundedCornerShape(50),
                    colors = CardDefaults.cardColors(containerColor = registro.corStatus),
                ) {
                    Text(
                        text = registro.status,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = White,
                        style = typography.labelMedium,
                    )
                }
            }
            Text(
                text = "#${registro.id.toString().padStart(6, '0')}",
                color = TextSecondary,
                style = typography.labelMedium,
            )
            registro.detalhes.forEach { detalhe ->
                Text(detalhe, color = TextPrimary, style = typography.bodyMedium)
            }
            Row(
                modifier = Modifier.align(Alignment.End),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    enabled = !isProcessando,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, GreenPrimary),
                ) {
                    Image(
                        painter = painterResource(iconeAcaoPrincipal),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        rotuloAcaoPrincipal,
                        modifier = Modifier.padding(start = 6.dp),
                        color = GreenPrimary,
                        style = typography.labelLarge,
                    )
                }
                if (mostrarAcaoDestrutiva) {
                    Button(
                        onClick = onDeactivate,
                        enabled = !isProcessando,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RedPrimary, contentColor = White),
                    ) {
                        Image(
                            painter = painterResource(Res.drawable.icon_excluir_light),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(rotuloAcaoDestrutiva, modifier = Modifier.padding(start = 6.dp), color = White, style = typography.labelLarge)
                    }
                }
            }
        }
    }
}
