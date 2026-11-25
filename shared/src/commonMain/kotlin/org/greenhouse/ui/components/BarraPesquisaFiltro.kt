@file:Suppress("ktlint:standard:function-naming")

package org.greenhouse.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import greenhouse.shared.generated.resources.Res
import greenhouse.shared.generated.resources.icon_filtro
import greenhouse.shared.generated.resources.icon_pesquisa
import org.greenhouse.ui.theme.GreenPrimary
import org.greenhouse.ui.theme.PinkSecondary
import org.greenhouse.ui.theme.TextPrimary
import org.jetbrains.compose.resources.painterResource

@Composable
fun <T> BarraPesquisaFiltro(
    busca: String,
    onBuscaChange: (String) -> Unit,
    placeholder: String,
    filtros: List<T>,
    rotuloFiltro: (T) -> String,
    onFiltroSelecionado: (T) -> Unit,
    descricaoFiltro: String,
    modifier: Modifier = Modifier,
) {
    var filtroExpandido by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        OutlinedTextField(
            value = busca,
            onValueChange = onBuscaChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text(placeholder, color = TextPrimary.copy(alpha = 0.65f)) },
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
                modifier = Modifier.size(52.dp).border(BorderStroke(1.dp, GreenPrimary), RoundedCornerShape(14.dp)),
            ) {
                Image(
                    painter = painterResource(Res.drawable.icon_filtro),
                    contentDescription = descricaoFiltro,
                    modifier = Modifier.size(26.dp),
                )
            }
            DropdownMenu(expanded = filtroExpandido, onDismissRequest = { filtroExpandido = false }) {
                filtros.forEach { filtro ->
                    DropdownMenuItem(
                        text = { Text(rotuloFiltro(filtro), color = TextPrimary) },
                        onClick = {
                            onFiltroSelecionado(filtro)
                            filtroExpandido = false
                        },
                    )
                }
            }
        }
    }
}
