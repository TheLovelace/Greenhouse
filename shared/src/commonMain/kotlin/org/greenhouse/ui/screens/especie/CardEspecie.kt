@file:Suppress("ktlint:standard:function-naming")

package org.greenhouse.ui.screens.especie

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
import greenhouse.shared.generated.resources.icon_especie
import greenhouse.shared.generated.resources.icon_excluir_light
import org.greenhouse.ui.screens.especie.model.EspecieItem
import org.greenhouse.ui.theme.AppTypography
import org.greenhouse.ui.theme.GreenPrimary
import org.greenhouse.ui.theme.GreenSecondary
import org.greenhouse.ui.theme.RedPrimary
import org.greenhouse.ui.theme.TextPrimary
import org.greenhouse.ui.theme.TextSecondary
import org.greenhouse.ui.theme.White
import org.jetbrains.compose.resources.painterResource

@Composable
fun CardEspecie(
    especie: EspecieItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val typography = AppTypography()

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
                            painter = painterResource(Res.drawable.icon_especie),
                            contentDescription = "Espécie",
                            modifier = Modifier.padding(9.dp).size(26.dp),
                        )
                    }
                }

                Card(
                    shape = RoundedCornerShape(50),
                    colors = CardDefaults.cardColors(containerColor = GreenSecondary),
                ) {
                    Text(
                        text = "#${especie.id.toString().padStart(6, '0')}",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = White,
                        style = typography.labelMedium,
                    )
                }
            }

            Text(
                text = especie.nomePopular,
                color = TextPrimary,
                style = typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            especie.descricao?.takeIf(String::isNotBlank)?.let {
                Text(text = it, color = TextPrimary, style = typography.bodyMedium)
            }
            Text(
                text = "Nome científico: ${especie.nomeCientifico?.takeIf(String::isNotBlank) ?: "Não informado"}",
                color = TextPrimary,
                style = typography.bodyMedium,
            )
            Text(
                text =
                    "Rega: ${especie.intervaloRegaDias?.let { "$it dia(s)" } ?: "Não informada"}  •  " +
                        "Temp.: ${especie.temperaturaMin ?: "-"}°C a ${especie.temperaturaMax ?: "-"}°C",
                color = TextPrimary,
                style = typography.bodyMedium,
            )
            Text(
                text =
                    "Umidade ar: ${especie.umidadeArMin ?: "-"}%–${especie.umidadeArMax ?: "-"}%  •  " +
                        "Solo: ${especie.umidadeSoloMin ?: "-"}%–${especie.umidadeSoloMax ?: "-"}%",
                color = TextPrimary,
                style = typography.bodyMedium,
            )
            Text(
                text = "Status: ${if (especie.ativo) "Ativa" else "Inativa"}",
                color = if (especie.ativo) TextPrimary else RedPrimary,
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
                    Text(
                        "Editar",
                        modifier = Modifier.padding(start = 6.dp),
                        color = GreenPrimary,
                        style = typography.labelLarge,
                    )
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