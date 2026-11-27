@file:Suppress("ktlint:standard:function-naming")

package org.greenhouse.ui.screens.alerta

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
import greenhouse.shared.generated.resources.icon_alerta
import greenhouse.shared.generated.resources.icon_editar
import org.greenhouse.ui.screens.alerta.model.AlertaItem
import org.greenhouse.ui.theme.AppTypography
import org.greenhouse.ui.theme.GreenPrimary
import org.greenhouse.ui.theme.GreenSecondary
import org.greenhouse.ui.theme.RedPrimary
import org.greenhouse.ui.theme.TextPrimary
import org.greenhouse.ui.theme.White
import org.jetbrains.compose.resources.painterResource

@Composable
fun CardAlerta(
    alerta: AlertaItem,
    onEdit: () -> Unit,
    onToggleStatus: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val typography = AppTypography()
    val condicoes =
        buildList {
            if (alerta.monitorarTemperatura) add("Temperatura")
            if (alerta.monitorarUmidadeAr) add("Umidade do ar")
            if (alerta.monitorarUmidadeSolo) add("Umidade do solo")
        }.joinToString(", ")

    val prioridadeTexto =
        when (alerta.prioridade) {
            1 -> "Alta (1)"
            2 -> "Média (2)"
            3 -> "Baixa (3)"
            else -> alerta.prioridade.toString()
        }

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
                            painter = painterResource(Res.drawable.icon_alerta),
                            contentDescription = "Alerta IoT",
                            modifier = Modifier.padding(9.dp).size(26.dp),
                        )
                    }
                }

                Card(
                    shape = RoundedCornerShape(50),
                    colors = CardDefaults.cardColors(containerColor = GreenSecondary),
                ) {
                    Text(
                        text = "#${alerta.id.toString().padStart(6, '0')}",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = White,
                        style = typography.labelMedium,
                    )
                }
            }

            Text(
                text = alerta.titulo,
                color = TextPrimary,
                style = typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text =
                    "Dispositivo: ${alerta.dispositivoNome.ifBlank { "#${alerta.dispositivoIotId}" }}  •  " +
                        "Espécie: ${alerta.especieNomePopular.ifBlank { "#${alerta.especieId}" }}",
                color = TextPrimary,
                style = typography.bodyMedium,
            )
            Text(
                text = "Prioridade: $prioridadeTexto  •  Monitora: $condicoes",
                color = TextPrimary,
                style = typography.bodyMedium,
            )
            alerta.mensagem?.takeIf(String::isNotBlank)?.let { msg ->
                Text(
                    text = msg,
                    color = TextPrimary,
                    style = typography.bodyMedium,
                )
            }
            Text(
                text = "Status: ${if (alerta.ativo) "Ativo" else "Inativo"}",
                color = if (alerta.ativo) TextPrimary else RedPrimary,
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
                    onClick = onToggleStatus,
                    shape = RoundedCornerShape(14.dp),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = if (alerta.ativo) RedPrimary else GreenPrimary,
                            contentColor = White,
                        ),
                ) {
                    Text(
                        text = if (alerta.ativo) "Inativar" else "Ativar",
                        color = White,
                        style = typography.labelLarge,
                    )
                }
            }
        }
    }
}