@file:Suppress("ktlint:standard:function-naming")

package org.greenhouse.ui.screens.estoque

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import greenhouse.shared.generated.resources.Res
import greenhouse.shared.generated.resources.icon_editar
import greenhouse.shared.generated.resources.icon_estoque
import greenhouse.shared.generated.resources.icon_excluir_light
import org.greenhouse.ui.screens.estoque.model.EstoqueItem
import org.greenhouse.ui.theme.AppTypography
import org.greenhouse.ui.theme.GreenPrimary
import org.greenhouse.ui.theme.GreenSecondary
import org.greenhouse.ui.theme.RedPrimary
import org.greenhouse.ui.theme.TextPrimary
import org.greenhouse.ui.theme.TextSecondary
import org.greenhouse.ui.theme.White
import org.jetbrains.compose.resources.painterResource

@Composable
fun CardEstoque(
    estoque: EstoqueItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val typography = AppTypography()
    val estoqueBaixo = estoque.quantidadeDisponivel <= estoque.quantidadeMinima

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, GreenPrimary),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Box(
                    modifier = Modifier.size(44.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Card(
                        shape = CircleShape,
                        border = BorderStroke(1.dp, GreenPrimary),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    ) {
                        Image(
                            painter = painterResource(Res.drawable.icon_estoque),
                            contentDescription = "Estoque",
                            modifier = Modifier.padding(9.dp).size(26.dp),
                        )
                    }
                }

                Card(
                    shape = RoundedCornerShape(50),
                    colors = CardDefaults.cardColors(containerColor = GreenSecondary),
                ) {
                    Text(
                        text = "#${estoque.produtoId.toString().padStart(6, '0')}",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = White,
                        style = typography.labelMedium,
                    )
                }
            }

            Text(
                text = "Produto #${estoque.produtoId}",
                color = TextPrimary,
                style = typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text =
                    "Físico: ${estoque.quantidadeFisica}  •  Reservado: ${estoque.quantidadeReservada}  •  Disponível: ${estoque.quantidadeDisponivel}",
                color = TextPrimary,
                style = typography.bodyMedium,
            )
            Text(
                text = "Quantidade mínima: ${estoque.quantidadeMinima}",
                color = TextPrimary,
                style = typography.bodyMedium,
            )
            Text(
                text = "Localização: ${estoque.localizacao?.takeIf(String::isNotBlank) ?: "Não informada"}",
                color = TextPrimary,
                style = typography.bodyMedium,
            )
            Text(
                text =
                    when {
                        estoque.bloqueado -> "Status: Bloqueado"
                        estoqueBaixo -> "Status: Disponível abaixo do mínimo"
                        else -> "Status: Desbloqueado"
                    },
                color = if (estoque.bloqueado || estoqueBaixo) RedPrimary else TextPrimary,
                style = typography.bodyMedium,
            )

            Row(
                modifier = Modifier.align(Alignment.End),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, GreenPrimary),
                ) {
                    Image(
                        painter = painterResource(Res.drawable.icon_editar),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Text("Editar", modifier = Modifier.padding(start = 6.dp), color = GreenPrimary, style = typography.labelLarge)
                }
                Button(
                    onClick = onDelete,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RedPrimary, contentColor = White),
                ) {
                    Image(
                        painter = painterResource(Res.drawable.icon_excluir_light),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        "Excluir",
                        modifier = Modifier.padding(start = 6.dp),
                        color = TextSecondary,
                        style = typography.labelLarge,
                    )
                }
            }
        }
    }
}
