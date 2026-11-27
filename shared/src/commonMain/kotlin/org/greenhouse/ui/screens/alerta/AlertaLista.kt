
@file:Suppress("ktlint:standard:function-naming")

package org.greenhouse.ui.screens.alerta

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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import greenhouse.shared.generated.resources.icon_filtro
import greenhouse.shared.generated.resources.icon_pesquisa
import greenhouse.shared.generated.resources.icon_voltar
import org.greenhouse.ui.components.Footer
import org.greenhouse.ui.screens.alerta.model.AlertaItem
import org.greenhouse.ui.theme.AppTypography
import org.greenhouse.ui.theme.Background
import org.greenhouse.ui.theme.GreenPrimary
import org.greenhouse.ui.theme.PinkSecondary
import org.greenhouse.ui.theme.TextPrimary
import org.greenhouse.ui.theme.TextSecondary
import org.jetbrains.compose.resources.painterResource

private enum class FiltroAlerta(
    val label: String,
) {
    TODOS("Todos"),
    ATIVOS("Ativos"),
    INATIVOS("Inativos"),
}

@Composable
fun AlertaLista(
    alertas: List<AlertaItem>,
    onBack: () -> Unit,
    onCreate: () -> Unit,
    onEdit: (AlertaItem) -> Unit,
    onToggleStatus: (AlertaItem) -> Unit,
    padding: PaddingValues = PaddingValues(),
    embedded: Boolean = false,
) {
    val typography = AppTypography()
    var busca by remember { mutableStateOf("") }
    var filtro by remember { mutableStateOf(FiltroAlerta.TODOS) }
    var filtroExpandido by remember { mutableStateOf(false) }
    var alertaParaAlterarStatus by remember { mutableStateOf<AlertaItem?>(null) }

    val alertasFiltrados =
        alertas.filter { alerta ->
            val termo = busca.trim()
            val correspondeBusca =
                termo.isEmpty() ||
                    alerta.id.toString().contains(termo, ignoreCase = true) ||
                    alerta.titulo.contains(termo, ignoreCase = true) ||
                    alerta.dispositivoNome.contains(termo, ignoreCase = true) ||
                    alerta.especieNomePopular.contains(termo, ignoreCase = true)
            val correspondeFiltro =
                when (filtro) {
                    FiltroAlerta.TODOS -> true
                    FiltroAlerta.ATIVOS -> alerta.ativo
                    FiltroAlerta.INATIVOS -> !alerta.ativo
                }
            correspondeBusca && correspondeFiltro
        }

    BoxWithConstraints(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Background)
                .padding(horizontal = 24.dp, vertical = 18.dp),
    ) {
        val compactLayout = maxWidth < 600.dp
        Column(modifier = Modifier.fillMaxSize()) {
            if (!embedded) {
                Box(modifier = Modifier.fillMaxWidth().height(52.dp), contentAlignment = Alignment.Center) {
                    Text("Alertas IoT", color = TextPrimary, style = typography.titleMedium)
                    IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart).size(48.dp)) {
                        Image(
                            painter = painterResource(Res.drawable.icon_voltar),
                            contentDescription = "Voltar",
                            modifier = Modifier.size(32.dp),
                        )
                    }
                }
                Spacer(Modifier.height(20.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = busca,
                    onValueChange = { busca = it },
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text("Buscar por ID, título, dispositivo ou espécie", color = TextPrimary.copy(alpha = 0.65f))
                    },
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
                        OutlinedTextFieldDefaults.colors(
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
                            contentDescription = "Filtrar alertas",
                            modifier = Modifier.size(26.dp),
                        )
                    }
                    DropdownMenu(expanded = filtroExpandido, onDismissRequest = { filtroExpandido = false }) {
                        FiltroAlerta.entries.forEach { opcao ->
                            DropdownMenuItem(
                                text = { Text(opcao.label, color = TextPrimary) },
                                onClick = {
                                    filtro = opcao
                                    filtroExpandido = false
                                },
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))
            if (alertasFiltrados.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.TopStart,
                ) {
                    Text(
                        text = if (alertas.isEmpty()) "Nenhum alerta IoT cadastrado." else "Nenhum alerta encontrado.",
                        color = TextPrimary,
                        style = typography.bodyLarge,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = if (embedded) 16.dp else 92.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    items(alertasFiltrados, key = { it.id }) { alerta ->
                        CardAlerta(
                            alerta = alerta,
                            onEdit = { onEdit(alerta) },
                            onToggleStatus = { alertaParaAlterarStatus = alerta },
                        )
                    }
                }
            }
            if (!embedded) {
                Spacer(Modifier.height(12.dp))
                Footer()
            }
        }

        if (!embedded) {
            FloatingActionButton(
                onClick = onCreate,
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
                        Text("Cadastrar alerta", color = TextSecondary, style = typography.labelLarge)
                    }
                }
            }
        }
    }

    alertaParaAlterarStatus?.let { alerta ->
        AlertDialog(
            onDismissRequest = {
                alertaParaAlterarStatus = null
            },
            title = {
                Text("Confirmar alteração")
            },
            text = {
                Text(
                    if (alerta.ativo) {
                        "Deseja realmente desativar este alerta?"
                    } else {
                        "Deseja realmente ativar este alerta?"
                    },
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onToggleStatus(alerta)
                        alertaParaAlterarStatus = null
                    },
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        alertaParaAlterarStatus = null
                    },
                ) {
                    Text("Cancelar")
                }
            },
        )
    }
}
