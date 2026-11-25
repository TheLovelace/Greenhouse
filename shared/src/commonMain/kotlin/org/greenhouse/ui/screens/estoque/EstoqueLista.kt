@file:Suppress("ktlint:standard:function-naming")

package org.greenhouse.ui.screens.estoque

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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import greenhouse.shared.generated.resources.Res
import greenhouse.shared.generated.resources.icon_adicionar_light
import greenhouse.shared.generated.resources.icon_filtro
import greenhouse.shared.generated.resources.icon_pesquisa
import greenhouse.shared.generated.resources.icon_voltar
import org.greenhouse.ui.components.Footer
import org.greenhouse.ui.screens.estoque.model.EstoqueItem
import org.greenhouse.ui.theme.AppTypography
import org.greenhouse.ui.theme.Background
import org.greenhouse.ui.theme.GreenPrimary
import org.greenhouse.ui.theme.PinkSecondary
import org.greenhouse.ui.theme.TextPrimary
import org.greenhouse.ui.theme.TextSecondary
import org.jetbrains.compose.resources.painterResource

private enum class FiltroEstoque(
    val label: String,
) {
    TODOS("Todos"),
    BLOQUEADOS("Bloqueados"),
    ABAIXO_DO_MINIMO("Abaixo do mínimo"),
}

@Composable
fun EstoqueLista(
    estoques: List<EstoqueItem>,
    onBack: () -> Unit,
    onCreate: () -> Unit,
    onEdit: (EstoqueItem) -> Unit,
    onDelete: (EstoqueItem) -> Unit,
) {
    val typography = AppTypography()
    var busca by remember { mutableStateOf("") }
    var filtro by remember { mutableStateOf(FiltroEstoque.TODOS) }
    var filtroExpandido by remember { mutableStateOf(false) }
    var estoqueParaExcluir by remember { mutableStateOf<EstoqueItem?>(null) }

    val estoquesFiltrados =
        estoques.filter { estoque ->
            val termo = busca.trim()
            val correspondeBusca =
                estoque.produtoId.toString().contains(termo, ignoreCase = true) ||
                    estoque.localizacao.orEmpty().contains(termo, ignoreCase = true)
            val correspondeFiltro =
                when (filtro) {
                    FiltroEstoque.TODOS -> true
                    FiltroEstoque.BLOQUEADOS -> estoque.bloqueado
                    FiltroEstoque.ABAIXO_DO_MINIMO -> estoque.quantidadeDisponivel <= estoque.quantidadeMinima
                }
            correspondeBusca && correspondeFiltro
        }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize().background(Background).padding(horizontal = 24.dp, vertical = 18.dp),
    ) {
        val compactLayout = maxWidth < 600.dp
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxWidth().height(52.dp), contentAlignment = Alignment.Center) {
                Text("Estoque", color = TextPrimary, style = typography.titleMedium)
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
                    value = busca,
                    onValueChange = { busca = it },
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text("Buscar por ID do produto ou localização", color = TextPrimary.copy(alpha = 0.65f))
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
                        androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
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
                            contentDescription = "Filtrar estoques",
                            modifier = Modifier.size(26.dp),
                        )
                    }
                    DropdownMenu(expanded = filtroExpandido, onDismissRequest = { filtroExpandido = false }) {
                        FiltroEstoque.entries.forEach { opcao ->
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
            if (estoquesFiltrados.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.TopStart,
                ) {
                    Text(
                        text = if (estoques.isEmpty()) "Nenhum registro de estoque cadastrado." else "Nenhum estoque encontrado.",
                        color = TextPrimary,
                        style = typography.bodyLarge,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 92.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    items(estoquesFiltrados, key = { it.produtoId }) { estoque ->
                        CardEstoque(
                            estoque = estoque,
                            onEdit = { onEdit(estoque) },
                            onDelete = { estoqueParaExcluir = estoque },
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Footer()
        }

        FloatingActionButton(
            onClick = onCreate,
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 10.dp, bottom = 48.dp),
            shape = RoundedCornerShape(18.dp),
            containerColor = GreenPrimary,
            contentColor = androidx.compose.ui.graphics.Color.White,
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
                    Text("Cadastrar estoque", color = TextSecondary, style = typography.labelLarge)
                }
            }
        }
    }

    estoqueParaExcluir?.let { estoque ->
        ModalConfirmacaoEstoque(
            estoque = estoque,
            onDismiss = { estoqueParaExcluir = null },
            onConfirm = {
                if (estoque.quantidadeReservada == 0) onDelete(estoque)
                estoqueParaExcluir = null
            },
        )
    }
}
